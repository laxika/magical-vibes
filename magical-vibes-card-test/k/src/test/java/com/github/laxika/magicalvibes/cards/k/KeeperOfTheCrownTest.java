package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoronationOfTheWilds;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfTheCrown.class, CoronationOfTheWilds.class, GrizzlyBears.class, Forest.class})
class KeeperOfTheCrownTest extends BaseCardTest {

    @Test
    void adventureMakesCreatureLegendaryNobleGrantsManaAndDraws() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfTheCrown());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);

        harness.setHand(player1, List.of(new CoronationOfTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.NOBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);

        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        harness.activateAbility(player1, targetIndex, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.getLegendarySourceManaTotal()).isEqualTo(1);
        assertThat(new ManaCost("{2}{L}").canPay(mana, 0)).isFalse();
        mana.add(ManaColor.COLORLESS, 2);
        assertThat(new ManaCost("{2}{L}").canPay(mana, 0)).isTrue();
        new ManaCost("{2}{L}").pay(mana, 0);
        assertThat(mana.getTotalAllMana()).isZero();
        assertThat(keeper).isNotNull();
    }

    @Test
    void adventureAllowsCastingKeeperWithManaFromItsLegendaryTarget() {
        KeeperOfTheCrown card = new KeeperOfTheCrown();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.BEAR)).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Keeper of the Crown");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void adventureManaAbilityPersistsAfterTheTurnEnds() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new KeeperOfTheCrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, target, CardSubtype.NOBLE)).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySourceManaTotal()).isEqualTo(1);
    }

    @Test
    void adventureCannotTargetOpponentsCreatureOrOwnLand() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new KeeperOfTheCrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castAdventure(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void illegalAdventureTargetPreventsDrawAndExilePermission() {
        KeeperOfTheCrown card = new KeeperOfTheCrown();
        Forest drawn = new Forest();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void keeperDoesNotBoostItselfEvenWhenLegendaryOrNonlegendaryCreatures() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfTheCrown());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new KeeperOfTheCrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, keeper.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, keeper, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.getEffectivePower(gd, keeper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keeper)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, keeper, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void ordinaryManaCannotPayLegendaryManaSymbol() {
        harness.setHand(player1, List.of(new KeeperOfTheCrown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
