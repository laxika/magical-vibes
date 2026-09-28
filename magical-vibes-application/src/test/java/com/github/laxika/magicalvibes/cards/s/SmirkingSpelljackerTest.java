package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SmirkingSpelljacker.class, GrizzlyBears.class})
class SmirkingSpelljackerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and exiles a spell an opponent controls")
    void entersAndExilesOpponentsSpell() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(spell.getId());
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(spell.getId());
    }

    @Test
    @DisplayName("Cannot target a spell controlled by its controller")
    void cannotTargetOwnSpell() {
        SmirkingSpelljacker spelljacker = new SmirkingSpelljacker();
        GrizzlyBears ownSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(spelljacker, ownSpell));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 6);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castCreature(player1, 1);
        harness.passPriority(player1);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking offers the exiled spell for free")
    void attackingOffersExiledSpellForFree() {
        GrizzlyBears spell = castOpponentSpellAndSpelljacker();
        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        Permanent source = harness.getGameData().playerBattlefields.get(player1.getId()).get(0);
        source.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    private GrizzlyBears castOpponentSpellAndSpelljacker() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new SmirkingSpelljacker()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return spell;
    }
}
