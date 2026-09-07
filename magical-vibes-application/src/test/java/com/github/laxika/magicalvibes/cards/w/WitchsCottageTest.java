package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchsCottage.class, Swamp.class, GrizzlyBears.class, HolyDay.class})
class WitchsCottageTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanThreeOtherSwamps() {
        addSwamp(player1);
        addSwamp(player1);

        playCottage();

        assertThat(findCottage(player1).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void entersUntappedAndOffersACreatureFromTheGraveyardWithThreeOtherSwamps() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        addSwamp(player1);
        addSwamp(player1);
        addSwamp(player1);

        playCottage();

        assertThat(findCottage(player1).isTapped()).isFalse();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void triggerStillResolvesIfCottageIsTappedAfterEnteringUntapped() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addSwamp(player1);
        addSwamp(player1);
        addSwamp(player1);

        playCottage();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        int cottageIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findCottage(player1));
        harness.activateAbility(player1, cottageIndex, 0, null, null);

        assertThat(findCottage(player1).isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
    }

    @Test
    void tapsForBlackMana() {
        Permanent cottage = harness.addToBattlefieldAndReturn(player1, new WitchsCottage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cottage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void playCottage() {
        harness.setHand(player1, List.of(new WitchsCottage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void addSwamp(Player player) {
        harness.addToBattlefield(player, new Swamp());
    }

    private Permanent findCottage(Player player) {
        return findPermanent(player, "Witch's Cottage");
    }
}
