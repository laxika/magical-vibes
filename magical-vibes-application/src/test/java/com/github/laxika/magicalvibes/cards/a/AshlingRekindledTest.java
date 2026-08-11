package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshlingRekindledTest extends BaseCardTest {

    @Test
    @DisplayName("Ashling rummages when it enters the battlefield")
    void rummagesOnEnter() {
        setDeck(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new AshlingRekindled(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(Card::getName).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Ashling transforms into Rimebound and creates its restricted mana")
    void transformsIntoRimebound() {
        Permanent ashling = addFrontFace(player1);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(ashling.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getManaValueAtLeastFourOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Rimebound's mana only casts spells with mana value at least four")
    void restrictedManaRequiresManaValueAtLeastFour() {
        Permanent ashling = addBackFace(player1);
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player1, List.of(new LightningBolt()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiant);
    }

    @Test
    @DisplayName("Rimebound transforms back into Rekindled after paying red")
    void transformsBackIntoRekindled() {
        Permanent ashling = addBackFace(player1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ashling.isTransformed()).isFalse();
    }

    private Permanent addFrontFace(Player player) {
        AshlingRekindled card = new AshlingRekindled();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addBackFace(Player player) {
        AshlingRekindled card = new AshlingRekindled();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
