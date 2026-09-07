package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperwoodElemental.class, GrizzlyBears.class, WrathOfGod.class})
class WhisperwoodElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card at the beginning of its controller's end step")
    void manifestsAtEndStep() {
        harness.addToBattlefield(player1, new WhisperwoodElemental());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Sacrifice ability grants death manifest only to face-up nontoken creatures")
    void sacrificeAbilityFiltersGrantedDeathTrigger() {
        Permanent whisperwood = addCreatureReady(player1, new WhisperwoodElemental());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent faceDownCreature = addCreatureReady(player1, new GrizzlyBears());
        faceDownCreature.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, tokenCreature());

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(manifested -> assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(whisperwood.getCard());
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setName("Soldier Token");
        token.setType(CardType.CREATURE);
        token.setPower(2);
        token.setToughness(2);
        token.setToken(true);
        return token;
    }
}
