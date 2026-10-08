package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SculptingSteel;
import com.github.laxika.magicalvibes.cards.v.VoldarenEpicure;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeddingSecurity.class, VoldarenEpicure.class, SculptingSteel.class})
class WeddingSecurityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Blood token on attack puts a counter on Wedding Security and draws a card")
    void sacrificingBloodTokenBoostsAndDraws() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = addBloodToken();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
    }

    @Test
    @DisplayName("Declining the Blood sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = addBloodToken();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blood);
    }

    @Test
    @DisplayName("Accepting without a Blood token does nothing")
    void noBloodTokenDoesNothing() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the controller's Blood tokens can be sacrificed")
    void cannotSacrificeOpponentsBloodToken() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = createBloodToken(player2);
        harness.setLibrary(player1, List.of(new WeddingSecurity()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blood);
    }

    @Test
    @DisplayName("One attack sacrifices only one Blood token and grants the reward immediately")
    void multipleBloodTokensGrantOnlyOneReward() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent firstBlood = createBloodToken(player1);
        harness.enterBattlefieldAndReturn(player1, new VoldarenEpicure());
        harness.passBothPriorities();
        Permanent secondBlood = findPermanents(player1, "Blood").stream()
                .filter(p -> !p.getId().equals(firstBlood.getId())).findFirst().orElseThrow();
        harness.setLibrary(player1, List.of(new WeddingSecurity(), new WeddingSecurity()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstBlood.getId());

        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondBlood).doesNotContain(firstBlood);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nontoken copy of Blood is not a legal sacrifice")
    void cannotSacrificeNontokenBloodCopy() {
        Permanent security = addCreatureReady(player1, new WeddingSecurity());
        Permanent blood = createBloodToken(player1);
        SculptingSteel steel = new SculptingSteel();
        harness.setHand(player1, List.of(steel));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getId().equals(steel.getId())).findFirst().orElseThrow();
        harness.setLibrary(player1, List.of(new WeddingSecurity()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(blood.getId());
        harness.handlePermanentChosen(player1, blood.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy).doesNotContain(blood);
        assertThat(security.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent createBloodToken(Player player) {
        harness.enterBattlefieldAndReturn(player, new VoldarenEpicure());
        harness.passBothPriorities();
        return findPermanent(player, "Blood");
    }

    private Permanent addBloodToken() {
        Card blood = new Card();
        blood.setName("Blood");
        blood.setType(CardType.ARTIFACT);
        blood.setSubtypes(List.of(CardSubtype.BLOOD));
        blood.setToken(true);
        return harness.addToBattlefieldAndReturn(player1, blood);
    }
}
