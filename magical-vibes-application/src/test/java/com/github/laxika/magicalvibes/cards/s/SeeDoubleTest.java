package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeeDouble.class, AngelsMercy.class, GrizzlyBears.class, Spellbook.class, Twincast.class})
class SeeDoubleTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a target instant spell")
    void copiesTargetInstantSpell() {
        AngelsMercy mercy = new AngelsMercy();
        SeeDouble seeDouble = new SeeDouble();
        harness.setHand(player1, List.of(mercy, seeDouble));
        addSeeDoubleMana(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        castSeeDouble(player1, new int[]{0}, mercy.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getDescription()).isEqualTo("Copy of Angel's Mercy");
            assertThat(entry.isCopy()).isTrue();
        });
    }

    @Test
    @DisplayName("A copied permanent spell becomes a token")
    void copiedPermanentSpellBecomesToken() {
        GrizzlyBears bears = new GrizzlyBears();
        SeeDouble seeDouble = new SeeDouble();
        harness.setHand(player1, List.of(bears, seeDouble));
        addSeeDoubleMana(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        castSeeDouble(player1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creates a creature token when the creature mode is chosen")
    void createsCreatureToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeeDouble()));
        addSeeDoubleMana(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Allows both modes when an opponent has eight cards in their graveyard")
    void allowsBothModesAtThreshold() {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            graveyard.add(new Spellbook());
        }
        harness.setGraveyard(player2, graveyard);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears bearsSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(bearsSpell, new SeeDouble()));
        addSeeDoubleMana(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        castSeeDouble(player1, new int[]{0, 1}, bearsSpell.getId(), List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getDescription()).isEqualTo("Copy of Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Rejects choosing both modes below the graveyard threshold")
    void rejectsBothModesBelowThreshold() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new SeeDouble()));
        addSeeDoubleMana(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> castSeeDouble(player1, new int[]{0, 1}, mercy.getId(),
                List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected between 1 and 1 modes");
    }

    @Test
    @DisplayName("Cannot be copied")
    void cannotBeCopied() {
        AngelsMercy mercy = new AngelsMercy();
        SeeDouble seeDouble = new SeeDouble();
        harness.setHand(player1, List.of(mercy, seeDouble));
        addSeeDoubleMana(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        castSeeDouble(player1, new int[]{0}, mercy.getId(), List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, seeDouble.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry -> "Copy of See Double".equals(entry.getDescription()));
    }

    private void addSeeDoubleMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void castSeeDouble(Player player, int[] modeIndices, java.util.UUID targetId,
                               List<java.util.UUID> targetIds) {
        gs.playCard(gd, player, 0, ChooseOneEffect.encodeModeSelection(1, 2, modeIndices),
                targetId, null, targetIds, List.of());
    }
}
