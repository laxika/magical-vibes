package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RotwidowPack.class, MotherBear.class, SnowCoveredSwamp.class})
class RotwidowPackTest extends BaseCardTest {

    @Test
    void activatedAbilityPromptsForCreatureCardToExile() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    void activatedAbilityCreatesSpiderAndEachOpponentLosesForAllControlledSpiders() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mother Bear"));

        Permanent token = findPermanent(player1, "Spider");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
    }

    @Test
    void activatedAbilityRequiresCreatureCardInGraveyard() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void exileIsPaidBeforeResolutionAndOpponentSpidersDoNotCount() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.addToBattlefield(player1, new MotherBear());
        harness.addToBattlefield(player2, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spider");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void abilityStillCreatesTokenAndCountsCurrentSpidersAfterSourceLeaves() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new MotherBear()));
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider");
        harness.assertLife(player2, 19);
    }

    @Test
    void canActivateTwiceWithoutTappingAndEachResolutionCountsNewTokens() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new MotherBear(), new MotherBear()));
        addManaForAbility();
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spider"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void cannotExileNoncreatureCardOrUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RotwidowPack());
        harness.setGraveyard(player1, List.of(new SnowCoveredSwamp()));
        harness.setGraveyard(player2, List.of(new MotherBear()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
