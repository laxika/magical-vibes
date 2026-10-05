package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HardHittingQuestion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TinStreetGossip;
import com.github.laxika.magicalvibes.cards.t.TorchTheWitness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudithCarnageConnoisseur.class, TinStreetGossip.class, Shock.class,
        TorchTheWitness.class, HardHittingQuestion.class})
class JudithCarnageConnoisseurTest extends BaseCardTest {

    private static final String KEYWORD_MODE = "That spell gains deathtouch and lifelink";
    private static final String IMP_MODE =
            "Create a 2/2 red Imp creature token with \"When this token dies, it deals 2 damage to each opponent.\"";

    @Test
    @DisplayName("Judith can give the cast spell deathtouch and lifelink")
    void givesCastSpellDeathtouchAndLifelink() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        Permanent gossip = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, gossip.getId());
        chooseJudithMode(KEYWORD_MODE);

        harness.assertNotOnBattlefield(player2, "Tin Street Gossip");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Judith can create an Imp whose death damages each opponent")
    void createsImpWithDeathTrigger() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        chooseJudithMode(IMP_MODE);

        Permanent imp = findPermanents(player1, "Imp").getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, imp.getId());
        chooseJudithMode(KEYWORD_MODE);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Imp")).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Judith grants sorceries deathtouch and lifelink, including excess damage")
    void grantsKeywordsToSorcery() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        Permanent gossip = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, gossip.getId());
        chooseJudithMode(KEYWORD_MODE);

        harness.assertNotOnBattlefield(player2, "Tin Street Gossip");
        harness.assertOnBattlefield(player1, "Clue");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Zero damage from a spell with deathtouch and lifelink does nothing")
    void zeroDamageDoesNotDestroyOrGainLife() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        Permanent gossip = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, gossip.getId());
        chooseJudithMode(KEYWORD_MODE);

        harness.assertOnBattlefield(player2, "Tin Street Gossip");
        harness.assertNotOnBattlefield(player1, "Clue");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Judith")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        Permanent gossip = harness.addToBattlefieldAndReturn(player1, new TinStreetGossip());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, gossip.getId());

        harness.assertOnBattlefield(player1, "Tin Street Gossip");
        assertThat(gossip.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Imp");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Judith")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        harness.setHand(player1, List.of(new TinStreetGossip()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tin Street Gossip");
        harness.assertNotOnBattlefield(player1, "Imp");
    }

    @Test
    @DisplayName("Spell keywords do not apply when the spell instructs a creature to deal damage")
    void doesNotGrantKeywordsToCreatureDealingDamage() {
        Permanent judith = harness.addToBattlefieldAndReturn(player1, new JudithCarnageConnoisseur());
        Permanent gossip = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new HardHittingQuestion()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(judith.getId(), gossip.getId()));
        chooseJudithMode(KEYWORD_MODE);

        harness.assertOnBattlefield(player2, "Tin Street Gossip");
        assertThat(gossip.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 20);
    }

    private void chooseJudithMode(String mode) {
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
