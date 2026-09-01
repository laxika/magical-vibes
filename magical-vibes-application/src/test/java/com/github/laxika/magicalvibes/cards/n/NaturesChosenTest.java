package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.k.KjeldoranOutpost;
import com.github.laxika.magicalvibes.cards.w.WildAesthir;
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

@CardUsed({NaturesChosen.class, WildAesthir.class, GorillaChieftain.class, AshnodsCylix.class,
        KjeldoranOutpost.class})
class NaturesChosenTest extends BaseCardTest {

    private Permanent enchanted;
    private Permanent aura;

    private void enchant(Permanent creature) {
        enchanted = creature;
        aura = harness.addToBattlefieldAndReturn(player1, new NaturesChosen());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    @DisplayName("{0} ability untaps the enchanted creature")
    void untapsEnchantedCreature() {
        enchant(addCreatureReady(player1, new GorillaChieftain()));
        enchanted.tap();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The {0} ability can be activated only once each turn")
    void untapAbilityIsOncePerTurn() {
        enchant(addCreatureReady(player1, new GorillaChieftain()));
        enchanted.tap();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        enchanted.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping a white enchanted creature untaps a target land")
    void tapWhiteCreatureToUntapLand() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutpost());
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(aura.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void tapWhiteCreatureToUntapArtifact() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        artifact.tap();

        harness.activateAbility(player1, 1, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void tapWhiteCreatureToUntapCreature() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        Permanent creature = addCreatureReady(player2, new GorillaChieftain());
        creature.tap();

        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap-target ability cannot be activated when the enchanted creature is not white")
    void requiresWhiteEnchantedCreature() {
        enchant(addCreatureReady(player1, new GorillaChieftain()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutpost());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap-target ability cannot be activated while the enchanted creature is tapped")
    void requiresUntappedEnchantedCreature() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        enchanted.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutpost());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap-target ability can be activated only once each turn")
    void untapTargetAbilityIsOncePerTurn() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutpost());
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untapAbilityIsOnlyDuringYourTurn() {
        enchant(addCreatureReady(player1, new GorillaChieftain()));
        enchanted.tap();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchanted.isTapped()).isTrue();
    }

    @Test
    void untapTargetAbilityRejectsEnchantmentTarget() {
        enchant(addCreatureReady(player1, new WildAesthir()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    void oncePerTurnLimitResetsOnNextTurn() {
        enchant(addCreatureReady(player1, new WildAesthir()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KjeldoranOutpost());
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new WildAesthir());
        harness.setHand(player1, List.of(new NaturesChosen()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
