package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardColor;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VioletPall.class, BurrentonBombardier.class, PricklyBoggart.class, Mutavault.class})
class VioletPallTest extends BaseCardTest {

    // "Destroy target nonblack creature. Create a 1/1 black Faerie Rogue creature token with flying."

    @Test
    @DisplayName("Destroys a nonblack creature and creates a flying Faerie Rogue token")
    void destroysNonblackCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new BurrentonBombardier());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID bombardierId = harness.getPermanentId(player2, "Burrenton Bombardier");
        harness.castInstant(player1, 0, List.of(bombardierId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burrenton Bombardier");
        List<Permanent> tokens = findPermanents(player1, "Faerie Rogue").stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE, CardSubtype.ROGUE);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.FLYING);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID blackId = harness.getPermanentId(player2, "Prickly Boggart");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(blackId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Prickly Boggart");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Mutavault());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID mutavaultId = harness.getPermanentId(player2, "Mutavault");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mutavaultId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mutavault");
    }
}
