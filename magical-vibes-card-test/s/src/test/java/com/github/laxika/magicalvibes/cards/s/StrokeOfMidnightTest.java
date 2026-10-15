package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MeteorGolem;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrokeOfMidnight.class, GrizzlyBears.class, LlanowarElves.class, Forest.class, MeteorGolem.class,
        DarksteelColossus.class})
class StrokeOfMidnightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonland permanent and gives its controller a 1/1 Human token")
    void destroysNonlandPermanentAndCreatesHumanToken() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrokeOfMidnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrokeOfMidnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyOwnArtifactAndCreatesTokenForItsController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new MeteorGolem()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrokeOfMidnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Meteor Golem");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().matches(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().getSubtypes().equals(List.of(CardSubtype.HUMAN))
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void createsTokenEvenWhenTargetIsIndestructible() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DarksteelColossus()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrokeOfMidnight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().matches(permanent -> permanent.getCard().getName().equals("Human"));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotCreateAnotherTokenWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StrokeOfMidnight(), new StrokeOfMidnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().matches(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human"));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
