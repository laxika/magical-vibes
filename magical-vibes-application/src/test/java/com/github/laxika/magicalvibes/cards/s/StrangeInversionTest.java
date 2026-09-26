package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        StrangeInversion.class,
        KamiOfAncientLaw.class,
        HumbleBudoka.class,
        ReachThroughMists.class,
        CounselOfTheSoratami.class,
        SenseisDiviningTop.class
})
class StrangeInversionTest extends BaseCardTest {

    @Test
    @DisplayName("Switches target creature's power and toughness")
    void switchesPowerAndToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KamiOfAncientLaw());
        creature.setPowerModifier(1); // 3/2
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StrangeInversion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KamiOfAncientLaw());
        creature.setPowerModifier(1); // 3/2
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StrangeInversion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // the temporary power modifier wears off alongside the switch
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SenseisDiviningTop());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StrangeInversion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        var targetId = harness.getPermanentId(player1, "Sensei's Divining Top");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot target a creature with shroud")
    void cannotTargetCreatureWithShroud() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StrangeInversion()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, budoka.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KamiOfAncientLaw());
        StrangeInversion inversion = new StrangeInversion();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), inversion));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, creature.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KamiOfAncientLaw());
        creature.setPowerModifier(1); // 3/2
        StrangeInversion inversion = new StrangeInversion();
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player1.getId()).add(new KamiOfAncientLaw());
        harness.setHand(player1, List.of(new ReachThroughMists(), inversion));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, creature.getId(), List.of(1));
        harness.passBothPriorities();

        // Reach Through Mists draws a card, then the spliced switch changes 3/2 to 2/3.
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(inversion);
    }
}
