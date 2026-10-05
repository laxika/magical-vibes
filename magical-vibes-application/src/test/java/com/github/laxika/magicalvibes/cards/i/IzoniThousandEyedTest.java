package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzoniThousandEyed.class, BartizanBats.class, DirectCurrent.class})
class IzoniThousandEyedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Insect for each creature card in the controller's graveyard")
    void createsInsectsForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new BartizanBats(), new BartizanBats(), new DirectCurrent()));
        harness.setGraveyard(player2, List.of(new BartizanBats()));
        harness.castFromHand(player1, new IzoniThousandEyed(), "{2}{B}{B}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .forEach(permanent -> {
                    assertThat(permanent.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
                    assertThat(permanent.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
                    assertThat(permanent.getCard().getPower()).isEqualTo(1);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                });
        harness.assertOnBattlefield(player1, "Izoni, Thousand-Eyed");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing another creature gains life and draws a card")
    void sacrificeAnotherCreatureGainsLifeAndDraws() {
        Permanent izoni = addReadyIzoni(player1);
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setLibrary(player1, List.of(new DirectCurrent()));
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Direct Current");
        harness.assertInGraveyard(player1, "Bartizan Bats");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(izoni);
    }

    @Test
    @DisplayName("Cannot sacrifice Izoni itself for its activated ability")
    void requiresAnotherCreature() {
        addReadyIzoni(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No Insects are created without creature cards in your graveyard")
    void noCreaturesInGraveyardCreatesNoTokens() {
        harness.setGraveyard(player1, List.of(new DirectCurrent()));
        harness.setGraveyard(player2, List.of(new BartizanBats()));
        harness.castFromHand(player1, new IzoniThousandEyed(), "{2}{B}{B}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();
        harness.assertOnBattlefield(player1, "Izoni, Thousand-Eyed");
    }

    @Test
    @DisplayName("Undergrowth counts creature cards when the trigger resolves")
    void countsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new BartizanBats()));
        harness.castFromHand(player1, new IzoniThousandEyed(), "{2}{B}{B}{G}{G}");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Insect")).isZero();
        harness.setGraveyard(player1, List.of(new BartizanBats(), new BartizanBats(), new DirectCurrent()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    @DisplayName("Undergrowth still resolves after Izoni leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.castFromHand(player1, new IzoniThousandEyed(), "{2}{B}{B}{G}{G}");
        harness.passBothPriorities();
        Permanent izoni = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(izoni);
        harness.setGraveyard(player1, List.of(izoni.getCard()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Izoni, Thousand-Eyed");
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activation and sacrifice is paid before resolution")
    void summoningSickIzoniCanActivate() {
        Permanent izoni = harness.addToBattlefieldAndReturn(player1, new IzoniThousandEyed());
        izoni.setSummoningSick(true);
        harness.addToBattlefield(player1, new BartizanBats());
        harness.setLibrary(player1, List.of(new DirectCurrent()));
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Bartizan Bats");
        harness.assertNotOnBattlefield(player1, "Bartizan Bats");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore + 1);
        harness.assertInHand(player1, "Direct Current");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addReadyIzoni(player1);
        harness.addToBattlefield(player2, new BartizanBats());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bartizan Bats");
    }

    @Test
    @DisplayName("A newly created Insect token can pay the sacrifice cost")
    void canSacrificeInsectToken() {
        harness.setGraveyard(player1, List.of(new BartizanBats()));
        harness.setLibrary(player1, List.of(new DirectCurrent()));
        harness.castFromHand(player1, new IzoniThousandEyed(), "{2}{B}{B}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(countPermanents(player1, "Insect")).isZero();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertInHand(player1, "Direct Current");
        harness.assertOnBattlefield(player1, "Izoni, Thousand-Eyed");
    }

    private Permanent addReadyIzoni(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IzoniThousandEyed());
        perm.setSummoningSick(false);
        return perm;
    }
}
