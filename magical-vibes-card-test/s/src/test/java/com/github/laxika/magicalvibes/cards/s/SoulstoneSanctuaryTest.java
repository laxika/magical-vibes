package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulstoneSanctuary.class})
class SoulstoneSanctuaryTest extends BaseCardTest {

    @Test
    void tappingProducesColorlessMana() {
        addSanctuaryReady(player1);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void resolvingAbilityMakesItA3x3VigilantCreatureWithAllCreatureTypes() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, sanctuary, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, sanctuary, CardSubtype.ZOMBIE)).isTrue();
    }

    @Test
    void animatedSanctuaryIsStillALand() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, sanctuary)).isTrue();
    }

    @Test
    void animationPersistsAfterEndOfTurn() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();
        assertThat(gqs.isLand(gd, sanctuary)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sanctuary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sanctuary, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, sanctuary, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, sanctuary, CardSubtype.ZOMBIE)).isTrue();
    }

    @Test
    void activatingAbilityConsumesFourManaWithoutTappingTheLand() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sanctuary.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    void canAnimateWhileTapped() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sanctuary.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sanctuary)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void animatedSanctuaryCanStillProduceMana() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(sanctuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void vigilantSanctuaryAttacksWithoutTapping() {
        Permanent sanctuary = addSanctuaryReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gd.declaredAttackerIdsThisCombat).contains(sanctuary.getId());
        assertThat(sanctuary.isTapped()).isFalse();
    }

    @Test
    void newlyControlledSanctuaryCanAnimateButCannotTapForManaAsACreature() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new SoulstoneSanctuary());
        sanctuary.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sanctuary)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(sanctuary.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addSanctuaryReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SoulstoneSanctuary());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
