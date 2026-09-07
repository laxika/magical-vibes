package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OteclanLandmark.class, OteclanLevitator.class, DarksteelRelic.class,
        GrizzlyBears.class, AirElemental.class})
class OteclanLandmarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by scrying two")
    void entersWithScryTwo() {
        harness.setHand(player1, List.of(new OteclanLandmark()));
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new DarksteelRelic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(topCard);
    }

    @Test
    @DisplayName("Craft returns Oteclan Landmark transformed")
    void craftReturnsTransformed() {
        harness.addToBattlefieldAndReturn(player1, new OteclanLandmark());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent levitator = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof OteclanLevitator)
                .findFirst().orElseThrow();
        assertThat(levitator.isTransformed()).isTrue();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Oteclan Levitator gives flying to an attacking creature without flying")
    void attackTriggerGivesFlyingToAttackingCreatureWithoutFlying() {
        OteclanLandmark front = new OteclanLandmark();
        Permanent levitator = new Permanent(front);
        levitator.setCard(front.getBackFaceCard());
        levitator.setTransformed(true);
        levitator.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(levitator);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        bears.setSummoningSick(false);
        airElemental.setSummoningSick(false);

        declareAttackers(player1, List.of(0, 1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }
}
