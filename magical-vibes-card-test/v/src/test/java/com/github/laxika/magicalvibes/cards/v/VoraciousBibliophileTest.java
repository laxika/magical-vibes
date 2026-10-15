package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CommonBond;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousBibliophile.class, CommonBond.class, Forest.class, GiantGrowth.class,
        HillGiant.class, HolyStrength.class, Shock.class, Counterspell.class})
class VoraciousBibliophileTest extends BaseCardTest {

    @Test
    void drawsForOnePermanentTarget() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsForOnePlayerTarget() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsForEachTargetOfMultiTargetSpell() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void stillDrawsWhenTriggeringSpellIsCounteredBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.assertInGraveyard(player1, "Shock");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerForSpellWithoutTargets() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForOpponentsTargetedSpell() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsForAuraSpellTarget() {
        Permanent bibliophile = harness.addToBattlefieldAndReturn(player1, new VoraciousBibliophile());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bibliophile.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Holy Strength");
    }

    @Test
    void drawsForSpellTarget() {
        harness.addToBattlefield(player1, new VoraciousBibliophile());
        HillGiant giant = new HillGiant();
        harness.setHand(player2, List.of(giant));
        harness.setHand(player1, List.of(new Counterspell()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
