package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LunaticPandora.class, Forest.class, GrizzlyBears.class})
class LunaticPandoraTest extends BaseCardTest {

    @Test
    void paysManaTapsAndSurveilsOne() {
        Permanent pandora = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(pandora.isTapped()).isTrue();
    }

    @Test
    void sacrificesItselfAndDestroysTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new LunaticPandora());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertInGraveyard(player1, "Lunatic Pandora");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetALand() {
        harness.addToBattlefield(player1, new LunaticPandora());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayLeaveSurveilledCardOnTopWithoutChangingLibraryOrder() {
        harness.addToBattlefield(player1, new LunaticPandora());
        Card topCard = new Forest();
        Card nextCard = new LunaticPandora();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void surveillingAnEmptyLibraryDoesNotLoseTheGame() {
        Permanent pandora = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(pandora.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Lunatic Pandora");
    }

    @Test
    void surveilTapCostPreventsActivatingDestructionUntilUntapped() {
        Permanent pandora = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LunaticPandora());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pandora.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Lunatic Pandora");
        harness.assertOnBattlefield(player2, "Lunatic Pandora");
    }

    @Test
    void canDestroyAnArtifactYouControl() {
        harness.addToBattlefield(player1, new LunaticPandora());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LunaticPandora).hasSize(2);
    }

    @Test
    void canTargetItselfAndPaysSacrificeBeforeResolution() {
        Permanent pandora = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, pandora.getId());

        harness.assertNotOnBattlefield(player1, "Lunatic Pandora");
        harness.assertInGraveyard(player1, "Lunatic Pandora");
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LunaticPandora).hasSize(1);
    }

    @Test
    void cannotActivateDestructionWithOnlyFiveMana() {
        harness.addToBattlefield(player1, new LunaticPandora());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LunaticPandora());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Lunatic Pandora");
        harness.assertNotInGraveyard(player1, "Lunatic Pandora");
        harness.assertOnBattlefield(player2, "Lunatic Pandora");
    }

    @Test
    void cannotActivateSurveilWithOnlyOneMana() {
        Permanent pandora = harness.addToBattlefieldAndReturn(player1, new LunaticPandora());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pandora.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Lunatic Pandora");
    }
}
