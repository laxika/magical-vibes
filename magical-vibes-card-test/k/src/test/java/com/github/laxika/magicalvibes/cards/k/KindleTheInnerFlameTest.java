package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindleTheInnerFlame.class, AirElemental.class, GrizzlyBears.class, Clone.class})
class KindleTheInnerFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty token copy of a creature you control and sacrifices it at end step")
    void createsHastyTokenCopyWithEndStepSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFromHand(harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        });
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KindleTheInnerFlame()));
        addFlashbackMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback can behold three distinct Elementals from the battlefield and hand")
    void flashbackBeholdsThreeDistinctElementals() {
        Permanent firstElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent secondElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        AirElemental handElemental = new AirElemental();
        KindleTheInnerFlame card = new KindleTheInnerFlame();
        harness.setHand(player1, List.of(handElemental));
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashbackWithBehold(player1, 0, targetId,
                List.of(firstElemental.getId(), secondElemental.getId()), List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstElemental, secondElemental);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handElemental);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    @Test
    void flashbackRequiresThreeElementals() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        KindleTheInnerFlame card = new KindleTheInnerFlame();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashbackWithBehold(player1, 0, elemental.getId(),
                List.of(elemental.getId()), List.of())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackCannotBeholdOneElementalThreeTimes() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new KindleTheInnerFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashbackWithBehold(player1, 0, elemental.getId(),
                List.of(elemental.getId(), elemental.getId(), elemental.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashbackCanRevealThreeElementalCardsWithoutConsumingThem() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        AirElemental first = new AirElemental();
        AirElemental second = new AirElemental();
        KindleTheInnerFlame revealed = new KindleTheInnerFlame();
        KindleTheInnerFlame spell = new KindleTheInnerFlame();
        harness.setHand(player1, List.of(first, second, revealed));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashbackWithBehold(player1, 0, target.getId(), List.of(), List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, revealed);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    void spellDoesNotCreateTokenIfTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KindleTheInnerFlame()));
        addFlashbackMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kindle the Inner Flame");
    }

    @Test
    void copyingTokenInheritsItsEndStepSacrificeAbility() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFromHand(original.getId());
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone")).findFirst().orElseThrow();

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(token, clone);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(original);
        });
    }

    private void castFromHand(UUID targetId) {
        harness.setHand(player1, List.of(new KindleTheInnerFlame()));
        addFlashbackMana();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
