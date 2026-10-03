package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.o.OtherworldlyJourney;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ChampionsHelm.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, OtherworldlyJourney.class})
class ChampionsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Equipped legendary creature has hexproof")
    void equippedLegendaryCreatureHasHexproof() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Bonuses are lost when Champion's Helm becomes unattached")
    void bonusesAreLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());

        helm.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches Champion's Helm to a creature you control")
    void equipAttachesToCreature() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reequippingTransfersBonusesToNonlegendaryCreature() {
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        helm.setAttachedTo(legendary.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void opponentCannotTargetEquippedLegendaryCreature() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new OtherworldlyJourney()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void controllerCanTargetEquippedLegendaryCreature() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new OtherworldlyJourney()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    void opponentCanTargetEquippedNonlegendaryCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new ChampionsHelm());
        helm.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new OtherworldlyJourney()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new ChampionsHelm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new ChampionsHelm());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
