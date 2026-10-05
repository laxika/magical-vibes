package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BattlefieldScavenger;
import com.github.laxika.magicalvibes.cards.k.KefnetsLastWord;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OasisRitualist.class, BattlefieldScavenger.class, KefnetsLastWord.class})
class OasisRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability taps and adds one mana of the chosen color without exerting")
    void firstAbilityAddsOneManaWithoutExert() {
        Permanent ritualist = addCreatureReady(player1, new OasisRitualist());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(ritualist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(ritualist.getSkipUntapCount()).isEqualTo(0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Second ability adds two mana of the chosen color and exerts")
    void secondAbilityAddsTwoManaAndExerts() {
        Permanent ritualist = addCreatureReady(player1, new OasisRitualist());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(ritualist.isTapped()).isTrue();
        assertThat(ritualist.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OasisRitualist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void exertSkipsOnlyNextOwnUntapStep() {
        Permanent ritualist = addCreatureReady(player1, new OasisRitualist());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.stack).isEmpty();
        harness.performUntapStep(player2);
        assertThat(ritualist.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(ritualist.isTapped()).isTrue();
        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(ritualist.isTapped()).isFalse();
    }

    @Test
    void exertAbilityCannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OasisRitualist());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @CardUsed({BattlefieldScavenger.class})
    void exertManaAbilityTriggersExertAbilities() {
        addCreatureReady(player1, new OasisRitualist());
        harness.addToBattlefield(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of(new OasisRitualist()));
        harness.setLibrary(player1, List.of(new OasisRitualist()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @CardUsed({KefnetsLastWord.class})
    void exertDoesNotPreventNewControllersUntap() {
        Permanent ritualist = addCreatureReady(player1, new OasisRitualist());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new KefnetsLastWord()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player2, 0, ritualist.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ritualist);

        harness.performUntapStep(player2);
        assertThat(ritualist.isTapped()).isFalse();
    }
}
