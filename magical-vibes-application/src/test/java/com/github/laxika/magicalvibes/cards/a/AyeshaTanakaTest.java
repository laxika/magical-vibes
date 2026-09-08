package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AyeshaTanakaTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact's activated ability when its controller cannot pay {W}")
    void countersArtifactAbilityWhenControllerCannotPay() {
        Permanent ayesha = addReadyAyesha();
        RodOfRuin rod = addRod();

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(ayesha.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The artifact ability resolves when its controller pays {W}")
    void artifactAbilityResolvesWhenControllerPaysWhite() {
        addReadyAyesha();
        RodOfRuin rod = addRod();
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a nonartifact source")
    void cannotTargetNonartifactAbility() {
        addReadyAyesha();
        ZuranSpellcaster spellcaster = new ZuranSpellcaster();
        harness.addToBattlefield(player2, spellcaster);
        findPermanent(player2, "Zuran Spellcaster").setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spellcaster.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAyesha() {
        Permanent ayesha = harness.addToBattlefieldAndReturn(player1, new AyeshaTanaka());
        ayesha.setSummoningSick(false);
        return ayesha;
    }

    private RodOfRuin addRod() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        findPermanent(player2, "Rod of Ruin").setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        return rod;
    }
}
