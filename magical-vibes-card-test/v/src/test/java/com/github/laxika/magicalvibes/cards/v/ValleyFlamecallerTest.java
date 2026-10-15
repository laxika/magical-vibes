package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BrambleguardVeteran;
import com.github.laxika.magicalvibes.cards.c.CoruscationMage;
import com.github.laxika.magicalvibes.cards.f.FinchFormation;
import com.github.laxika.magicalvibes.cards.h.HiredClaw;
import com.github.laxika.magicalvibes.cards.s.SeedglaiveMentor;
import com.github.laxika.magicalvibes.cards.s.SugarCoat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyFlamecaller.class, HiredClaw.class, BrambleguardVeteran.class, CoruscationMage.class,
        FinchFormation.class, SeedglaiveMentor.class, SugarCoat.class})
class ValleyFlamecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts matching combat and noncombat damage")
    void boostsMatchingCombatAndNoncombatDamage() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new HiredClaw());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveCombat(player1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not boost a creature with an unrelated subtype")
    void doesNotBoostUnrelatedSubtype() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, testCreature(CardSubtype.BIRD));
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void boostsItsOwnCombatDamage() {
        addCreatureReady(player1, new ValleyFlamecaller());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void multipleFlamecallersEachIncreaseDamage() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new ValleyFlamecaller());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void boostsMouseOtterAndRaccoonDamage() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new SeedglaiveMentor());
        addCreatureReady(player1, new CoruscationMage());
        addCreatureReady(player1, new BrambleguardVeteran());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1, 2, 3));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    void doesNotBoostOpponentsMatchingCreature() {
        harness.addToBattlefield(player1, new ValleyFlamecaller());
        addCreatureReady(player2, new BrambleguardVeteran());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void boostsDamageDealtToACreature() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player2, new BrambleguardVeteran());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Valley Flamecaller");
        harness.assertInGraveyard(player2, "Brambleguard Veteran");
    }

    @Test
    void doesNotBoostRealBirdCreature() {
        addCreatureReady(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new FinchFormation());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void stopsBoostingDamageAfterLosingItsAbilities() {
        Permanent flamecaller = harness.addToBattlefieldAndReturn(player1, new ValleyFlamecaller());
        addCreatureReady(player1, new BrambleguardVeteran());
        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, flamecaller.getId());
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Card testCreature(CardSubtype subtype) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(2);
        return card;
    }
}
