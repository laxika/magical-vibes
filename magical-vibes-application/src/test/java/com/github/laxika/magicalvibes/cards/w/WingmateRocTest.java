package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WingmateRocTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a 3/4 white Bird token with flying")
    void raidCreatesBirdToken() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castWingmateRoc();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Bird");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Raid does not create a Bird token when no attack occurred")
    void noRaidDoesNotCreateBirdToken() {
        castWingmateRoc();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("Attacking gains one life for each attacking creature")
    void gainsLifeForEachAttackingCreature() {
        addCreatureReady(player1, new WingmateRoc());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void castWingmateRoc() {
        harness.setHand(player1, List.of(new WingmateRoc()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
