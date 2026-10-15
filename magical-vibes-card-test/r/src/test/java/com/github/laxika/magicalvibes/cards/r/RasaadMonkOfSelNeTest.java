package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.d.DragonsFire;
import com.github.laxika.magicalvibes.cards.g.GrimBounty;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RasaadMonkOfSelNe.class, SteadfastPaladin.class, Mountain.class, Plains.class, GrimBounty.class, DragonsFire.class, Island.class, Swamp.class, Forest.class})
class RasaadMonkOfSelNeTest extends BaseCardTest {

    @Test
    void exilesAnOpponentsCreatureAndReturnsItWhenRasaadLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        Permanent rasaad = findPermanent(player1, "Rasaad, Monk of Selûne");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DragonsFire()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rasaad.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Steadfast Paladin"));
    }

    @Test
    void radiantFaceMakesTheExiledCreatureAnAbilitylessOneOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card exiled = gd.findExiledCard(target.getCard().getId()).card();
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, exiled.getId());
        harness.passBothPriorities();

        Card modified = gd.findExiledCard(exiled.getId()).card();
        assertThat(modified.getPower()).isEqualTo(1);
        assertThat(modified.getToughness()).isEqualTo(1);
        assertThat(modified.getCardText()).isEmpty();
        assertThat(modified.getActivatedAbilities()).isEmpty();
        assertThat(modified.getKeywords()).doesNotContain(Keyword.LIFELINK);
    }

    @Test
    void warriorFaceCreatesThreeSoldiers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rasaad, Warrior Monk")).isNotNull();
        Permanent warrior = findPermanent(player1, "Rasaad, Warrior Monk");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrimBounty()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, warrior.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(3);
    }

    @Test
    void returnsTheCapturedCreatureImmediatelyWhenTheBaseFaceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rasaad = findPermanent(player1, "Rasaad, Monk of Selûne");
        harness.setHand(player2, List.of(new DragonsFire()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, rasaad.getId());

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Steadfast Paladin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotExileTheTargetIfRasaadLeavesBeforeItsEntryTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent rasaad = findPermanent(player1, "Rasaad, Monk of Selûne");
        harness.setHand(player2, List.of(new DragonsFire()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, rasaad.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @ParameterizedTest
    @CsvSource({"1, Faerie Dragon, 2, 1, 1, FLYING", "2, Skeleton, 1, 4, 1, MENACE", "4, Boar, 2, 2, 2, NONE"})
    void specializedDeathAbilitiesCreateTheirTokens(int abilityIndex, String tokenName, int count,
                                                   int power, int toughness, String keyword) {
        Card discard = switch (abilityIndex) {
            case 1 -> new Island();
            case 2 -> new Swamp();
            default -> new Forest();
        };
        harness.addToBattlefield(player1, new RasaadMonkOfSelNe());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent specialized = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrimBounty()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, specialized.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals(tokenName))
                .hasSize(count)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(power);
                    assertThat(token.getCard().getToughness()).isEqualTo(toughness);
                    if (!keyword.equals("NONE")) {
                        assertThat(token.getCard().getKeywords()).contains(Keyword.valueOf(keyword));
                    }
                });
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "2", "3", "4"})
    void capturedCreatureReturnsWhenASpecializedFaceLeaves(int abilityIndex) {
        Card discard = switch (abilityIndex) {
            case 0 -> new Plains();
            case 1 -> new Island();
            case 2 -> new Swamp();
            case 3 -> new Mountain();
            default -> new Forest();
        };
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), discard));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (abilityIndex == 0) {
            harness.handlePermanentChosen(player1, target.getCard().getId());
            harness.passBothPriorities();
        }

        Permanent specialized = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrimBounty()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player2, 0, specialized.getId());

        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
        harness.assertOnBattlefield(player2, "Steadfast Paladin");
        if (abilityIndex == 0) {
            Permanent returned = findPermanent(player2, "Steadfast Paladin");
            assertThat(returned.getCard().getPower()).isEqualTo(1);
            assertThat(returned.getCard().getToughness()).isEqualTo(1);
            assertThat(returned.getCard().getKeywords()).doesNotContain(Keyword.LIFELINK);
        }
    }

    @Test
    void canSpecializeByDiscardingAColoredNonlandCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SteadfastPaladin());
        harness.setHand(player1, List.of(new RasaadMonkOfSelNe(), new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Steadfast Paladin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rasaad, Radiant Monk");
        assertThat(gd.findExiledCard(target.getCard().getId()).card().getPower()).isEqualTo(1);
    }
}
