package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.d.DreamLeash;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SvogthosTheRestlessTomb.class, BorosRecruit.class, Forest.class, DreamLeash.class})
class SvogthosTheRestlessTombTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Svogthos produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent svogthos = addSvogthosReady(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(svogthos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Svogthos becomes a black and green Plant Zombie with power and toughness equal to creature cards in its controller's graveyard")
    void activatesAsGraveyardSizedCreature() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new BorosRecruit(), new BorosRecruit(), new Forest()));
        harness.setGraveyard(player2, List.of(new BorosRecruit(), new BorosRecruit(), new BorosRecruit()));
        activate();

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        assertThat(gqs.isLand(gd, svogthos)).isTrue();
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, svogthos))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(svogthos.getTransientSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PLANT, CardSubtype.ZOMBIE);
        assertThat(svogthos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Svogthos continuously tracks creature cards entering and leaving its controller's graveyard")
    void tracksGraveyardChangesDuringAnimation() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new BorosRecruit(), new Forest()));
        activate();

        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(1);
        harness.setGraveyard(player1, List.of(new BorosRecruit(), new BorosRecruit(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new BorosRecruit(), new Forest()));
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(1);
    }

    @Test
    @DisplayName("Svogthos dies as a 0/0 creature when its controller has no creature cards in their graveyard")
    void diesWithNoCreatureCardsInGraveyard() {
        addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new Forest()));

        activate();

        harness.assertNotOnBattlefield(player1, "Svogthos, the Restless Tomb");
        harness.assertInGraveyard(player1, "Svogthos, the Restless Tomb");
    }

    @Test
    @DisplayName("Svogthos stops being animated at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new BorosRecruit()));
        activate();

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, svogthos)).isFalse();
        assertThat(gqs.isLand(gd, svogthos)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, svogthos)).isEmpty();
        assertThat(svogthos.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Animated Svogthos counts its new controller's graveyard after control changes")
    void followsNewControllersGraveyard() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new BorosRecruit()));
        harness.setGraveyard(player2, List.of(new BorosRecruit(), new BorosRecruit(), new Forest()));
        activate();
        harness.tapPermanent(player1, 0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DreamLeash()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, svogthos.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(svogthos);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(svogthos);
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(2);
        harness.setGraveyard(player2, List.of(new BorosRecruit(), new BorosRecruit(), new BorosRecruit()));
        assertThat(gqs.getEffectivePower(gd, svogthos)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, svogthos)).isEqualTo(3);
    }

    @Test
    @DisplayName("Svogthos can animate while tapped and retains its mana ability")
    void animatesWhileTappedAndRetainsManaAbility() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.setGraveyard(player1, List.of(new BorosRecruit()));
        harness.tapPermanent(player1, 0);

        activate();

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        assertThat(svogthos.isTapped()).isTrue();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        svogthos.untap();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
    }

    @Test
    @DisplayName("Animation requires both black and green mana")
    void animationRequiresBothColors() {
        Permanent svogthos = addSvogthosReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, svogthos)).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new BorosRecruit()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Newly entered Svogthos can animate but cannot tap for mana while a creature")
    void summoningSicknessDoesNotPreventAnimation() {
        Permanent svogthos = harness.addToBattlefieldAndReturn(player1, new SvogthosTheRestlessTomb());
        svogthos.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new BorosRecruit()));

        activate();

        assertThat(gqs.isCreature(gd, svogthos)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(svogthos.isTapped()).isFalse();
    }

    private void activate() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addSvogthosReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SvogthosTheRestlessTomb());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
