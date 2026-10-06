package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirsdagFlayer.class, HeadlessSkaab.class, EvolvingWilds.class,
        ArtificialEvolution.class, Bitterblossom.class})
class SkirsdagFlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Human destroys target creature and taps the Flayer")
    void sacrificeHumanDestroysTargetCreature() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new SkirsdagFlayer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());

        addAbilityMana(player1);

        int flayerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(flayer);
        harness.activateAbility(player1, flayerIdx, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, human.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(human);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(flayer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(flayer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Headless Skaab");
        harness.assertInGraveyard(player2, "Headless Skaab");
        harness.assertInGraveyard(player1, "Skirsdag Flayer");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer);
    }

    @Test
    @DisplayName("Can sacrifice the Flayer itself when it is the only Human")
    void canSacrificeItselfWhenOnlyHuman() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());

        addAbilityMana(player1);

        int flayerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(flayer);
        harness.activateAbility(player1, flayerIdx, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skirsdag Flayer");
        harness.assertInGraveyard(player1, "Skirsdag Flayer");
        harness.assertNotOnBattlefield(player2, "Headless Skaab");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void abilityRequiresMana() {
        addReadyFlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        // Missing {3} generic mana

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyFlayer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyFlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());

        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Skirsdag Flayer");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent flayer = addReadyFlayer(player1);
        flayer.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent flayer = harness.addToBattlefieldAndReturn(player1, new SkirsdagFlayer());
        flayer.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(flayer.isTapped()).isFalse();
    }

    @Test
    void canSacrificeTappedSummoningSickHuman() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new SkirsdagFlayer());
        human.setTapped(true);
        human.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer).doesNotContain(human);
        harness.assertInGraveyard(player1, "Skirsdag Flayer");
        harness.assertInGraveyard(player2, "Headless Skaab");
    }

    @Test
    void sacrificeChoicesExcludeNonHumansAndOpponentsPermanents() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new SkirsdagFlayer());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new HeadlessSkaab());
        Permanent opposingHuman = harness.addToBattlefieldAndReturn(player2, new SkirsdagFlayer());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, opposingHuman.getId());
        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(flayer.getId(), human.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, human.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer, nonHuman).doesNotContain(human);
        harness.assertInGraveyard(player2, "Skirsdag Flayer");
    }

    @Test
    void canTargetAndSacrificeItself() {
        Permanent flayer = addReadyFlayer(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, flayer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skirsdag Flayer");
        harness.assertInGraveyard(player1, "Skirsdag Flayer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    void canSacrificeNoncreatureHumanPermanent() {
        Permanent flayer = addReadyFlayer(player1);
        Permanent kindred = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeadlessSkaab());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, kindred.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "HUMAN");
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, kindred.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flayer).doesNotContain(kindred);
        harness.assertInGraveyard(player1, "Bitterblossom");
        harness.assertInGraveyard(player2, "Headless Skaab");
    }

    private Permanent addReadyFlayer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SkirsdagFlayer());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

}
