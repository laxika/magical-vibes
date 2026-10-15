package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrondTheDawnClad.class, HolyStrength.class, Forest.class, Demystify.class, Unsummon.class})
class KrondTheDawnCladTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted, attacking Krond exiles a target permanent")
    void enchantedAttackExilesTargetPermanent() {
        Permanent krond = addCreatureReady(player1, new KrondTheDawnClad());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        enchantKrond(krond);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Attacking Krond does not trigger while it is unenchanted")
    void unenchantedAttackDoesNotTrigger() {
        addCreatureReady(player1, new KrondTheDawnClad());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Removing the Aura before resolution prevents Krond's attack trigger")
    void removingAuraBeforeResolutionPreventsExile() {
        Permanent krond = addCreatureReady(player1, new KrondTheDawnClad());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        enchantKrond(krond);
        Permanent aura = findPermanent(player1, "Holy Strength");

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forest.getId());

        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Krond's trigger uses its last enchanted status after it leaves the battlefield")
    void removingEnchantedKrondDoesNotPreventExile() {
        Permanent krond = addCreatureReady(player1, new KrondTheDawnClad());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        enchantKrond(krond);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, forest.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, krond.getId());
        harness.assertNotOnBattlefield(player1, "Krond the Dawn-Clad");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Krond can exile the Aura enchanting it")
    void attackCanExileItsOwnAura() {
        Permanent krond = addCreatureReady(player1, new KrondTheDawnClad());
        enchantKrond(krond);
        Permanent aura = findPermanent(player1, "Holy Strength");

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Krond the Dawn-Clad");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Holy Strength"));
    }

    private void enchantKrond(Permanent krond) {
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, krond.getId());
        harness.passBothPriorities();
    }

}
