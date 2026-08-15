package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OviyaPashiriSageLifecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 1/1 colorless Servo artifact creature token")
    void firstAbilityCreatesServoToken() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(oviya), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Servo").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SERVO);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(oviya.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability creates a Construct whose power and toughness equal the controlled creature count")
    void secondAbilityCreatesConstructSizedToCreatureCount() {
        Permanent oviya = addCreatureReady(player1, new OviyaPashiriSageLifecrafter());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(oviya), 1, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Construct");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getColor()).isNull();
        assertThat(tokens.getFirst().getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
