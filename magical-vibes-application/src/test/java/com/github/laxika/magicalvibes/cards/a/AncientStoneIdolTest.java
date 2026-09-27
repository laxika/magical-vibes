package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientStoneIdol.class, GrizzlyBears.class})
class AncientStoneIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each attacking creature across all battlefields")
    void costIsReducedForEachAttackingCreature() {
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker2 = addCreatureReady(player2, new GrizzlyBears());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        harness.setHand(player1, List.of(new AncientStoneIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creates a trampling Construct token when it dies")
    void createsConstructWhenItDies() {
        Permanent idol = addCreatureReady(player1, new AncientStoneIdol());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, idol));
        harness.passBothPriorities();

        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(1);

        Permanent construct = constructs.getFirst();
        assertThat(construct.getCard().getPower()).isEqualTo(6);
        assertThat(construct.getCard().getToughness()).isEqualTo(12);
        assertThat(construct.getCard().getColor()).isNull();
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(construct.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(construct.getCard().getKeywords()).containsExactly(Keyword.TRAMPLE);
    }
}
