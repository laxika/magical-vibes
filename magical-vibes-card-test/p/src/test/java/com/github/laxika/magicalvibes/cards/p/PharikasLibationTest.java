package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikasLibation.class, GiantSpider.class, GrizzlyBears.class, PhyrexianArena.class, NyxbornColossus.class})
class PharikasLibationTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode makes the target opponent choose a creature to sacrifice")
    void creatureModeSacrificesChosenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(0, player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, spider.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Enchantment mode makes the target opponent choose an enchantment to sacrifice")
    void enchantmentModeSacrificesChosenEnchantment() {
        Permanent arena = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.addToBattlefield(player2, new PhyrexianArena());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(1, player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(arena.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(arena.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    @DisplayName("The modes cannot target the spell's controller")
    void modesCannotTargetController() {
        harness.setHand(player1, List.of(new PharikasLibation()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode resolves against an opponent with an empty battlefield")
    void eitherModeResolvesWithoutEligiblePermanents(int modeIndex) {
        harness.addToBattlefield(player1, new NyxbornColossus());

        cast(modeIndex, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Pharika's Libation");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("A mode does not sacrifice a permanent of the other type")
    void selectedModeDoesNotFallBackToOtherType(int modeIndex) {
        if (modeIndex == 0) {
            harness.addToBattlefield(player2, new PhyrexianArena());
        } else {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        cast(modeIndex, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, modeIndex == 0 ? "Phyrexian Arena" : "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pharika's Libation");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Either mode sacrifices the opponent's sole enchantment creature")
    void eitherModeSacrificesEnchantmentCreature(int modeIndex) {
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.addToBattlefield(player2, new NyxbornColossus());

        cast(modeIndex, player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "Nyxborn Colossus");
        harness.assertInGraveyard(player2, "Nyxborn Colossus");
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Pharika's Libation");
    }

    @Test
    @DisplayName("Enchantment mode cannot target the spell's controller")
    void enchantmentModeCannotTargetController() {
        harness.setHand(player1, List.of(new PharikasLibation()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void cast(int modeIndex, Player targetPlayer) {
        harness.setHand(player1, List.of(new PharikasLibation()));
        addMana(player1);
        harness.castModalInstant(player1, 0, modeIndex, List.of(targetPlayer.getId()));
        harness.passBothPriorities();
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
