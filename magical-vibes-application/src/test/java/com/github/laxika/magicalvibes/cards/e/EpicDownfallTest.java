package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FireNationWarship;
import com.github.laxika.magicalvibes.cards.o.OstrichHorse;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpicDownfall.class, FireNationWarship.class, OstrichHorse.class, OtterPenguin.class})
class EpicDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with mana value 3 or greater")
    void exilesCreatureWithManaValueThreeOrGreater() {
        Permanent target = addCreatureReady(player2, new OstrichHorse());
        castEpicDownfall(target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with mana value less than 3")
    void cannotTargetCreatureWithManaValueLessThanThree() {
        Permanent target = addCreatureReady(player2, new OtterPenguin());

        assertThatThrownBy(() -> castEpicDownfall(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FireNationWarship());

        assertThatThrownBy(() -> castEpicDownfall(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with mana value 3 or greater");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new OstrichHorse());
        castEpicDownfall(target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can exile a creature you control")
    void exilesOwnCreature() {
        Permanent target = addCreatureReady(player1, new OstrichHorse());
        castEpicDownfall(target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a face-down creature whose printed mana value is three")
    void cannotTargetFaceDownCreature() {
        Permanent target = addCreatureReady(player2, new OstrichHorse());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThatThrownBy(() -> castEpicDownfall(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or greater");
    }

    @Test
    @DisplayName("Does not exile a creature turned face down before resolution")
    void targetBecomingFaceDownIsIllegalOnResolution() {
        Permanent target = addCreatureReady(player2, new OstrichHorse());
        castEpicDownfall(target.getId());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Epic Downfall");
    }

    @Test
    @DisplayName("Exiles a crewed Vehicle without triggering its death ability")
    void exilesCrewedVehicleWithoutDeathTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FireNationWarship());
        addCreatureReady(player1, new OstrichHorse());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        castEpicDownfall(target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .noneMatch(permanent -> permanent.getCard() instanceof FireNationWarship);
    }

    private void castEpicDownfall(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EpicDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetId);
    }
}
