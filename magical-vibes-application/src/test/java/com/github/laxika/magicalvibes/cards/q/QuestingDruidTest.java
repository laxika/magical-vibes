package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.cards.b.BetrothTheBeast;
import com.github.laxika.magicalvibes.cards.c.ConceitedWitch;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.m.MockingSprite;
import com.github.laxika.magicalvibes.cards.s.SharaeOfNumbingDepths;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeekTheBeast;
import com.github.laxika.magicalvibes.cards.p.PriceOfBeauty;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TimeStretch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestingDruid.class, SeekTheBeast.class, Forest.class, GrizzlyBears.class, Shock.class,
        BesottedKnight.class, ConceitedWitch.class, Gingerbrute.class, MockingSprite.class,
        SharaeOfNumbingDepths.class, QuickStudy.class, BetrothTheBeast.class, PriceOfBeauty.class, TimeStretch.class})
class QuestingDruidTest extends BaseCardTest {

    @Test
    void adventureExilesTopTwoCardsUntilNextEndStep() {
        QuestingDruid card = new QuestingDruid();
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void castingAColoredSpellPutsACounterOnQuestingDruid() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingAGreenSpellDoesNotPutACounterOnQuestingDruid() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adventurePlayPermissionExpiresWhenNextEndStepBegins() {
        QuestingDruid card = new QuestingDruid();
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK"})
    void eachOtherListedColorTriggersBeforeTheSpellResolves(ManaColor color) {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        Card spell = switch (color) {
            case WHITE -> new BesottedKnight();
            case BLUE -> new MockingSprite();
            case BLACK -> new ConceitedWitch();
            default -> throw new IllegalArgumentException("Unexpected color");
        };
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, color, 1);
        harness.addMana(player1, ManaColor.COLORLESS, color == ManaColor.WHITE ? 3 : 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, spell.getName());
    }

    @Test
    void castingWhiteBlueSpellTriggersOnlyOnce() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        harness.setHand(player1, List.of(new SharaeOfNumbingDepths()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void castingColorlessCreatureDoesNotTriggerDruid() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        harness.setHand(player1, List.of(new Gingerbrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Gingerbrute");
    }

    @Test
    void opponentsRedAdventureDoesNotTriggerDruid() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        harness.setHand(player2, List.of(new QuestingDruid()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAdventure(player2, 0, List.of());
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void redAdventureTriggersDruidEvenThoughItsCreatureFaceIsGreen() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuestingDruid());
        QuestingDruid adventure = new QuestingDruid();
        harness.setHand(player1, List.of(adventure));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(adventure);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, adventure.getId());
        harness.passBothPriorities();
        assertThat(druid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void adventureAllowsPlayingALandAndAPaidCreatureFromExile() {
        QuestingDruid creature = new QuestingDruid();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new QuestingDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Questing Druid");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature, land);
    }

    @Test
    void exiledInstantCannotBeCastOnceNextEndStepHasBegun() {
        QuickStudy instant = new QuickStudy();
        harness.setLibrary(player1, List.of(instant, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new QuestingDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(instant);
    }

    @Test
    void adventureDuringOwnEndStepKeepsPermissionThroughOpponentsTurn() {
        QuickStudy instant = new QuickStudy();
        harness.forceStep(TurnStep.END_STEP);
        harness.setLibrary(player1, List.of(instant, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new QuestingDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, instant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quick Study");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(instant);
    }

    @Test
    void opponentsExtraTurnsDoNotExpirePermissionBeforeControllersNextEndStep() {
        QuickStudy instant = new QuickStudy();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TimeStretch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.castAndResolveSorcery(player2, 0, player2.getId());

        harness.setLibrary(player1, List.of(instant, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new QuestingDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, instant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quick Study");
    }
}
