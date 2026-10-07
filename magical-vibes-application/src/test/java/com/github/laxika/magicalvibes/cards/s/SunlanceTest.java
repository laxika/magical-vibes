package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.n.NumotTheDevastator;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.cards.w.WhitemaneLion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sunlance.class, CitanulWoodreaders.class, KavuPredator.class, SaltfieldRecluse.class,
        UrborgTombOfYawgmoth.class, NumotTheDevastator.class, WhitemaneLion.class})
class SunlanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target nonwhite creature")
    void dealsDamageToNonwhiteCreature() {
        Permanent target = addCreatureReady(player2, new KavuPredator());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kavu Predator");
        harness.assertInGraveyard(player2, "Kavu Predator");
    }

    @Test
    @DisplayName("Deals exactly 3 damage to a nonwhite creature that survives")
    void dealsExactlyThreeDamageToNonwhiteCreature() {
        Permanent target = addCreatureReady(player2, new CitanulWoodreaders());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Citanul Woodreaders");
    }

    @Test
    @DisplayName("Cannot target a white creature")
    void cannotTargetWhiteCreature() {
        Permanent target = addCreatureReady(player2, new SaltfieldRecluse());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite creature");
    }

    @Test
    @DisplayName("Can target a nonwhite creature you control")
    void canTargetOwnNonwhiteCreature() {
        Permanent target = addCreatureReady(player1, new KavuPredator());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Kavu Predator");
        harness.assertInGraveyard(player1, "Kavu Predator");
        harness.assertInGraveyard(player1, "Sunlance");
    }

    @Test
    @DisplayName("Cannot target a multicolored creature that is white")
    void cannotTargetWhiteMulticoloredCreature() {
        Permanent target = addCreatureReady(player2, new NumotTheDevastator());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite creature");
    }

    @Test
    @DisplayName("Does not deal damage when its target returns to hand before resolution")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = addCreatureReady(player2, new KavuPredator());
        harness.setHand(player1, List.of(new Sunlance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.castFromHand(player2, new WhitemaneLion(), "{1}{W}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player2, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Kavu Predator");
        harness.assertNotInGraveyard(player2, "Kavu Predator");
        harness.assertOnBattlefield(player2, "Whitemane Lion");
        assertThat(findPermanent(player2, "Whitemane Lion").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Sunlance");
        assertThat(gd.stack).isEmpty();
    }
}
