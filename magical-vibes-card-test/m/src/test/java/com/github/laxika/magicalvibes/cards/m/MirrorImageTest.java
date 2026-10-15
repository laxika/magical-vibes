package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DwarvenPriest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorImage.class, AirElemental.class, GrizzlyBears.class, DwarvenPriest.class})
class MirrorImageTest extends BaseCardTest {

    private void castMirrorImage() {
        harness.castFromHand(player1, new MirrorImage(), "{2}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Mirror Image enters as a copy of a creature its controller controls")
    void copiesOwnCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        harness.handlePermanentChosen(player1, elementalId);

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Mirror Image"))
                .findFirst().orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(copy.getCard().getPower()).isEqualTo(4);
        assertThat(copy.getCard().getToughness()).isEqualTo(4);
        assertThat(copy.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Mirror Image cannot copy a creature an opponent controls")
    void cannotCopyOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player1, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(elementalId)
                .doesNotContain(bearsId);
    }

    @Test
    @DisplayName("Mirror Image enters as a 0/0 and dies when the controller declines to copy")
    void diesWhenDeclining() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castMirrorImage();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Mirror Image"));
        harness.assertInGraveyard(player1, "Mirror Image");
    }

    @Test
    @DisplayName("Mirror Image dies without a copy choice when no creatures are controlled")
    void diesWithNoCreaturesToCopy() {
        castMirrorImage();

        harness.assertNotOnBattlefield(player1, "Mirror Image");
        harness.assertInGraveyard(player1, "Mirror Image");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature alone does not provide a copy choice")
    void diesWhenOnlyOpponentControlsCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMirrorImage();

        harness.assertInGraveyard(player1, "Mirror Image");
        harness.assertNotOnBattlefield(player1, "Mirror Image");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The copy choice happens before entry and does not use the stack")
    void copyChoiceIsAnEntryReplacement() {
        harness.addToBattlefield(player1, new AirElemental());
        castMirrorImage();

        harness.assertNotOnBattlefield(player1, "Mirror Image");
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Air Elemental"));

        assertThat(countPermanents(player1, "Air Elemental")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copying does not copy counters, damage, or tapped status")
    void doesNotCopyPermanentState() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent original = findPermanent(player1, "Air Elemental");
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.setMarkedDamage(1);
        castMirrorImage();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent copy = findPermanents(player1, "Air Elemental").stream()
                .filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mirror Image triggers the copied creature's entry ability")
    void triggersCopiedEntryAbility() {
        harness.addToBattlefield(player1, new DwarvenPriest());
        harness.setLife(player1, 20);
        castMirrorImage();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Dwarven Priest"));

        assertThat(countPermanents(player1, "Dwarven Priest")).isEqualTo(2);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Copying an existing Mirror Image copies its current copiable creature")
    void copiesAnExistingCopy() {
        harness.addToBattlefield(player1, new AirElemental());
        UUID originalId = harness.getPermanentId(player1, "Air Elemental");
        castMirrorImage();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, originalId);
        Permanent firstCopy = findPermanents(player1, "Air Elemental").stream()
                .filter(p -> !p.getId().equals(originalId))
                .findFirst().orElseThrow();

        castMirrorImage();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        assertThat(countPermanents(player1, "Air Elemental")).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
