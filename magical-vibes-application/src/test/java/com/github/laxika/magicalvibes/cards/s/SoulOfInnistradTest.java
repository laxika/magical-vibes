package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
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

@CardUsed({SoulOfInnistrad.class, RuneclawBear.class, SignInBlood.class})
class SoulOfInnistradTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability returns three target creature cards from your graveyard to your hand")
    void returnsThreeCreatureCards() {
        Permanent soul = addReadySoul(player1);
        Card first = new RuneclawBear();
        Card second = new RuneclawBear();
        Card third = new RuneclawBear();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("\"Up to three\" allows returning fewer creature cards")
    void returnsFewerThanThree() {
        Permanent soul = addReadySoul(player1);
        Card bears = new RuneclawBear();
        Card other = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bears, other));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bears.getId()).doesNotContain(other.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(other.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent soul = addReadySoul(player1);
        Card spell = new SignInBlood();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(spell.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent soul = addReadySoul(player1);
        Card bears = new RuneclawBear();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target more than three creature cards")
    void cannotTargetFourCards() {
        Permanent soul = addReadySoul(player1);
        Card a = new RuneclawBear();
        Card b = new RuneclawBear();
        Card c = new RuneclawBear();
        Card d = new RuneclawBear();
        harness.setGraveyard(player1, List.of(a, b, c, d));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(a.getId(), b.getId(), c.getId(), d.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Graveyard ability exiles this card as a cost and returns the targeted creature cards")
    void graveyardAbilityExilesSelfAndReturnsCards() {
        Card soul = new SoulOfInnistrad();
        Card first = new RuneclawBear();
        Card second = new RuneclawBear();
        harness.setGraveyard(player1, List.of(soul, first, second));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
    }

    @Test
    @DisplayName("Graveyard ability may target itself, but that target is gone once the exile cost is paid")
    void graveyardAbilityTargetingItselfFizzlesForThatCard() {
        Card soul = new SoulOfInnistrad();
        Card bears = new RuneclawBear();
        harness.setGraveyard(player1, List.of(soul, bears));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(soul.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bears.getId()).doesNotContain(soul.getId());
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
    }

    @Test
    @DisplayName("Battlefield ability can be activated with zero targets while summoning sick and tapped")
    void battlefieldAbilityAllowsZeroTargetsWithoutTapping() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfInnistrad());
        soul.setSummoningSick(true);
        soul.setTapped(true);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0, List.of());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(soul.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Graveyard ability with zero targets exiles its source immediately")
    void graveyardAbilityAllowsZeroTargetsAndPaysExileCostImmediately() {
        Card soul = new SoulOfInnistrad();
        harness.setGraveyard(player1, List.of(soul));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(handIds(player1)).doesNotContain(soul.getId());
    }

    @Test
    @DisplayName("Cannot select the same creature card twice")
    void cannotChooseDuplicateTargets() {
        Permanent soul = addReadySoul(player1);
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(handIds(player1)).doesNotContain(bear.getId());
    }

    @Test
    @DisplayName("An ability still returns remaining targets after another target leaves the graveyard")
    void returnsRemainingLegalTargets() {
        Permanent soul = addReadySoul(player1);
        Card first = new RuneclawBear();
        Card second = new RuneclawBear();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0,
                List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(second.getId()).doesNotContain(first.getId());
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(first.getId()));
    }

    @Test
    @DisplayName("Battlefield ability resolves after its source leaves the battlefield")
    void resolvesWithoutItsBattlefieldSource() {
        Permanent soul = addReadySoul(player1);
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(bear));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0, List.of(bear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(soul);
        gd.playerGraveyards.get(player1.getId()).add(soul.getCard());
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bear.getId()).doesNotContain(soul.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul.getCard());
    }

    @Test
    @DisplayName("Graveyard ability cannot be paid with only one black mana")
    void graveyardAbilityRequiresTwoBlackMana() {
        Card soul = new SoulOfInnistrad();
        Card bear = new RuneclawBear();
        harness.setGraveyard(player1, List.of(soul, bear));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul, bear);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(soul.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard ability targeting only itself returns nothing after paying its exile cost")
    void graveyardAbilityWithOnlySelfTargetReturnsNothing() {
        Card soul = new SoulOfInnistrad();
        harness.setGraveyard(player1, List.of(soul));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(soul.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).doesNotContain(soul.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Illegal graveyard targets are rejected before exiling the source as a cost")
    void graveyardAbilityRejectsNoncreatureBeforePayingExileCost() {
        Card soul = new SoulOfInnistrad();
        Card spell = new SignInBlood();
        harness.setGraveyard(player1, List.of(soul, spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(spell.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul, spell);
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getId().equals(soul.getId()));
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySoul(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SoulOfInnistrad());
        perm.setSummoningSick(false);
        return perm;
    }

    private int index(Permanent soul) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(soul);
    }

    private List<UUID> handIds(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
