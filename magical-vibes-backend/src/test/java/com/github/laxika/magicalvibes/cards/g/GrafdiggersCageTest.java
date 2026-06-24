package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromLibrariesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrafdiggersCageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has three STATIC effects: can't-enter, can't-cast-from-graveyards, can't-cast-from-libraries")
    void hasStaticEffects() {
        GrafdiggersCage card = new GrafdiggersCage();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect)
                .singleElement()
                .satisfies(e -> assertThat(((CardsCantEnterBattlefieldFromGraveyardsAndLibrariesEffect) e).filter())
                        .isEqualTo(new CardTypePredicate(CardType.CREATURE)));
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PlayersCantCastSpellsFromGraveyardsEffect);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PlayersCantCastSpellsFromLibrariesEffect);
    }

    // ===== Players can't cast spells from graveyards =====

    @Test
    @DisplayName("Prevents flashback casting from graveyards while on the battlefield")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback indices are empty while Grafdigger's Cage is on the battlefield")
    void flashbackIndicesAreEmpty() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        List<Integer> playable = harness.getGameBroadcastService()
                .getPlayableFlashbackIndices(gd, player2.getId());
        assertThat(playable).isEmpty();
    }

    // ===== Creature cards in graveyards can't enter the battlefield =====

    @Test
    @DisplayName("Blocks reanimation: a creature card stays in the graveyard")
    void blocksReanimationFromGraveyard() {
        harness.setGraveyard(player1, List.of(testCreature()));
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        harness.handleGraveyardCardChosen(player1, 0);

        // The creature card could not enter the battlefield and stays in the graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without the Cage, the same reanimation puts the creature onto the battlefield")
    void reanimationWorksWithoutCage() {
        harness.setGraveyard(player1, List.of(testCreature()));
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Players can't cast spells from libraries =====

    @Test
    @DisplayName("Prevents casting a spell from the top of a library")
    void preventsCastingFromLibraryTop() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player1, new GrafdiggersCage());
        gd.playerDecks.get(player1.getId()).addFirst(new Shock());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("libraries");
    }

    @Test
    @DisplayName("Without the Cage, casting from the top of a library is allowed")
    void castingFromLibraryTopWorksWithoutCage() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        var bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatCode(() -> harness.castFromLibraryTop(player1, bearsId)).doesNotThrowAnyException();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);
    }

    // ===== Helpers =====

    private Card testCreature() {
        return new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
