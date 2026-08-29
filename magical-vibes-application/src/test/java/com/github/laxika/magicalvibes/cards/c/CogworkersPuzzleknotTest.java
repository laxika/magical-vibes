package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CogworkersPuzzleknotTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 colorless Servo artifact creature token")
    void enteringBattlefieldCreatesServoToken() {
        harness.setHand(player1, List.of(new CogworkersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cogworker's Puzzleknot")).hasSize(1);
        assertThat(findPermanents(player1, "Servo")).hasSize(1);
        assertServo(findPermanent(player1, "Servo"));
    }

    @Test
    @DisplayName("Sacrificing it creates another Servo token")
    void sacrificeAbilityCreatesServoToken() {
        Permanent puzzleknot = harness.addToBattlefieldAndReturn(player1, new CogworkersPuzzleknot());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int puzzleknotIndex = gd.playerBattlefields.get(player1.getId()).indexOf(puzzleknot);
        harness.activateAbility(player1, puzzleknotIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(puzzleknot);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(puzzleknot.getCard());
        assertThat(findPermanents(player1, "Servo")).hasSize(1);
        assertServo(findPermanent(player1, "Servo"));
    }

    private void assertServo(Permanent token) {
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(token.getCard().isToken()).isTrue();
    }
}
