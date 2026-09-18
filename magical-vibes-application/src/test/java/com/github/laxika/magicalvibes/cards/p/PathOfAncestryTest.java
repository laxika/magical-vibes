package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfAncestry.class, GrizzlyBears.class, LlanowarElves.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    void entersTapped() {
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());

        assertThat(path.isTapped()).isTrue();
    }

    @Test
    void scriesWhenCommanderTypeCreatureUsesPathMana() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void doesNotTriggerForCreatureCastWithManaFromAnotherSource() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerForCreatureThatDoesNotShareCommanderType() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card commander() {
        Card card = new Card();
        card.setName("Test Bear Commander");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setManaCost("{1}");
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setPower(2);
        card.setToughness(2);
        card.setOwnerId(player1.getId());
        card.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(card)));
        return card;
    }
}
