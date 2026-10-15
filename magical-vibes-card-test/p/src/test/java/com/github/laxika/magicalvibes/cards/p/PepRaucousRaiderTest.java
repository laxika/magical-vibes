package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({PepRaucousRaider.class, GrizzlyBears.class, Forest.class, Shock.class})
class PepRaucousRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("A creature's combat damage exiles the damaged player's top card for play this turn")
    void combatDamageExilesTopCardWithPlayPermission() {
        addPep();
        addAttacker();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("A nonland permanent becomes an artifact without gaining Pep's mana ability")
    void nonlandPermanentBecomesArtifactWithoutGainingManaAbility() {
        addPep();
        addAttacker();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        Permanent artifactCreature = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().getId().equals(topCard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isArtifact(gd, artifactCreature)).isTrue();

        artifactCreature.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(artifactCreature);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Pep can sacrifice a stolen artifact creature for three mana of one chosen color")
    void pepSacrificesArtifactForMana() {
        Permanent pep = addPep();
        addAttacker();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        Permanent artifact = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().getId().equals(topCard.getId()))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pep), null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(pep.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Pep's own combat damage triggers the exile ability")
    void pepCombatDamageTriggersExile() {
        Permanent pep = addPep();
        pep.setAttacking(true);
        pep.setAttackTarget(player2.getId());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        harness.assertLife(player2, 17);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Each creature dealing combat damage exiles a separate card")
    void multipleCreaturesTriggerSeparately() {
        addPep();
        addAttacker();
        addAttacker();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("An empty damaged player's library produces no play permission")
    void emptyLibraryDoesNothing() {
        addPep();
        addAttacker();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An exiled land is playable but does not become an artifact")
    void exiledLandRemainsLand() {
        addPep();
        addAttacker();
        Card land = new Forest();
        land.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(land));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isArtifact(gd, forest)).isFalse();
        assertThat(gd.perpetualCardTypes).doesNotContainKey(land.getId());
        assertThat(gd.perpetualActivatedAbilities).doesNotContainKey(land.getId());
    }

    @Test
    @DisplayName("An exiled instant is playable without a perpetual artifact change")
    void exiledInstantRemainsInstant() {
        addPep();
        addAttacker();
        Card instant = new Shock();
        instant.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(instant));
        resolveCombatAndTrigger();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, instant.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.perpetualCardTypes).doesNotContainKey(instant.getId());
        assertThat(gd.perpetualActivatedAbilities).doesNotContainKey(instant.getId());
    }

    @Test
    @DisplayName("Combat damage from an opponent's creature does not trigger Pep")
    void opponentCombatDamageDoesNotTrigger() {
        addPep();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Play permission expires at end of turn while the artifact change remains")
    void playPermissionExpiresButArtifactChangePersists() {
        addPep();
        addAttacker();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        resolveCombatAndTrigger();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.perpetualCardTypes.get(topCard.getId()))
                .contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger the exile ability")
    void noncombatDamageDoesNotTrigger() {
        addPep();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Pep cannot pay its sacrifice cost with a nonartifact creature")
    void manaAbilityRequiresArtifact() {
        addPep();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPep() {
        return addCreatureReady(player1, new PepRaucousRaider());
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
