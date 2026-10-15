package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.k.KyoshiBattleFan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirbendingLesson.class, Forest.class, KnowledgeSeeker.class, KyoshiBattleFan.class})
class AirbendingLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends a nonland permanent and draws a card")
    void airbendsPermanentAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId()))
                .isEqualTo(player2.getId());
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The target's owner can cast an airbent permanent for {2}")
    void targetOwnerCanCastAirbentPermanentForTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, target.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Knowledge Seeker");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Airbends a noncreature artifact and still draws")
    void airbendsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KyoshiBattleFan());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kyoshi Battle Fan");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw when the only target leaves before resolution")
    void illegalTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getOriginalCard());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("Airbending a stolen permanent grants casting permission to its owner")
    void stolenPermanentCanBeRecastByOwner() {
        KnowledgeSeeker card = new KnowledgeSeeker();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Knowledge Seeker");
        harness.assertNotOnBattlefield(player1, "Knowledge Seeker");
    }

    @Test
    @DisplayName("Airbend permission does not let a creature be cast on the opponent's turn")
    void airbendPreservesNormalCastingTiming() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, target.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Casting an airbent permanent requires the full two mana")
    void airbendDoesNotAllowFreeCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, target.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
