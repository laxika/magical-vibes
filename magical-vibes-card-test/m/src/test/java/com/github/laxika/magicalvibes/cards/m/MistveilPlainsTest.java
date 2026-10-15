package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistveilPlains.class, SafeholdElite.class, GoldenglowMoth.class, DevotedDruid.class, GreaterAuramancy.class})
class MistveilPlainsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target graveyard card on the bottom of the library with two or more white permanents")
    void tucksTargetToBottomOfLibrary() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth()); // the Plains is colorless, so these two are the pair
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new DevotedDruid();
        harness.setGraveyard(player1, List.of(tucked));
        harness.setLibrary(player1, List.of(new GoldenglowMoth(), new DevotedDruid()));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(library.size() - 1).getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Cannot activate with fewer than two white permanents")
    void rejectedWithTooFewWhitePermanents() {
        Permanent plains = addPlains(player1);
        // One white permanent — the colorless Plains does not make up the second.
        addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new DevotedDruid();
        harness.setGraveyard(player1, List.of(tucked));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        UUID tuckedId = tucked.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tuckedId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-white permanents do not count toward the activation restriction")
    void nonWhitePermanentsDoNotCount() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new DevotedDruid()); // green — does not count
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new DevotedDruid();
        harness.setGraveyard(player1, List.of(tucked));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        UUID tuckedId = tucked.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tuckedId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds white mana")
    void manaAbilityAddsWhite() {
        Permanent plains = addPlains(player1);

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        harness.activateAbility(player1, plainsIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new MistveilPlains()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Mistveil Plains").isTapped()).isTrue();
    }

    @Test
    void activationPaysWhiteManaAndTapsLand() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new MistveilPlains();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));

        assertThat(plains.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new DevotedDruid();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsWhitePermanentsDoNotCount() {
        addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player2, new GoldenglowMoth());
        Card target = new DevotedDruid();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesAfterWhitePermanentsLeave() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new DevotedDruid();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != plains);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    @Test
    void doesNotMoveTargetThatLeftGraveyard() {
        addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new DevotedDruid();
        Card other = new GoldenglowMoth();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void whiteNoncreaturePermanentCounts() {
        addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        harness.addToBattlefield(player1, new GreaterAuramancy());
        Card target = new DevotedDruid();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    @Test
    void cannotActivateTappedLand() {
        Permanent plains = addPlains(player1);
        plains.tap();
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new DevotedDruid();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        addPlains(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new GoldenglowMoth());
        Card target = new DevotedDruid();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPlains(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MistveilPlains());
    }
}
