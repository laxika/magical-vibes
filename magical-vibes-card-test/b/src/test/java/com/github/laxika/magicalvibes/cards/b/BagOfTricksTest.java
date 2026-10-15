package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD8EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BagOfTricks.class, Forest.class, GrizzlyBears.class})
class BagOfTricksTest extends BaseCardTest {

    private RollD8EffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(RollD8EffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void rollsAndPutsCreatureWithMatchingManaValueOntoBattlefield() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2));
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfTricks());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bag.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest");
    }

    @Test
    void putsAllRevealedCardsOnBottomWhenNoCreatureMatchesTheRoll() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3));
        harness.addToBattlefieldAndReturn(player1, new BagOfTricks());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears", "Forest");
    }

    @Test
    void skipsMatchingNoncreatureAndStopsAtFirstMatchingCreature() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2));
        harness.addToBattlefield(player1, new BagOfTricks());
        BagOfTricks revealedArtifact = new BagOfTricks();
        Forest revealedLand = new Forest();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Forest unrevealedLand = new Forest();
        harness.setLibrary(player1, List.of(revealedArtifact, revealedLand, firstCreature,
                secondCreature, unrevealedLand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(firstCreature.getId()))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(revealedArtifact.getId())
                        || permanent.getCard().getId().equals(secondCreature.getId()));
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.subList(0, 2)).containsExactly(secondCreature, unrevealedLand);
        assertThat(library.subList(2, 4)).containsExactlyInAnyOrder(revealedArtifact, revealedLand);
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawingOrUsingOpponentsLibrary() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2));
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfTricks());
        harness.setLibrary(player1, List.of());
        GrizzlyBears opponentsCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentsCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bag.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCreature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
    private static final class FixedDiceRollService extends DiceRollService {

        private final int result;

        private FixedDiceRollService(int result) {
            this.result = result;
        }

        @Override
        public int roll(int sides) {
            return result;
        }
    }
}
