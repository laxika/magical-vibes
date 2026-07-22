package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GluttonousGuestTest extends BaseCardTest {

    @Test
    @DisplayName("When Gluttonous Guest enters, one Blood token is created")
    void etbCreatesOneBloodToken() {
        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bloods = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .toList();
        assertThat(bloods).hasSize(1);
        Permanent blood = bloods.getFirst();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Blood token draws a card and Guest gains 1 life")
    void bloodSacrificeDrawsAndGainsLife() {
        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();
        int bloodIdx = gd.playerBattlefields.get(player1.getId()).indexOf(blood);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bloodIdx, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Blood"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Sacrificing a non-Blood permanent does not gain life")
    void nonBloodSacrificeDoesNotGainLife() {
        harness.addToBattlefieldAndReturn(player1, new GluttonousGuest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        Card creature = new Card();
        creature.setName("Goblin Token");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of(CardSubtype.GOBLIN));
        creature.setPower(1);
        creature.setToughness(1);
        creature.setToken(true);
        Permanent goblin = new Permanent(creature);
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerGraveyards.get(player1.getId()).add(goblin.getCard());
        harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), goblin.getCard());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life when a Blood token is sacrificed")
    void gainsLifeWhenBloodSacrificed() {
        harness.addToBattlefieldAndReturn(player1, new GluttonousGuest());
        addBloodToken(player1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        int bloodIndex = -1;
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Blood")) {
                bloodIndex = i;
                break;
            }
        }
        assertThat(bloodIndex).isGreaterThanOrEqualTo(0);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bloodIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private void addBloodToken(Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setColor(null);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
