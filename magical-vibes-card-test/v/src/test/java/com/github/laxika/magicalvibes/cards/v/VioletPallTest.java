package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.d.Disperse;
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

@CardUsed({VioletPall.class, BurrentonBombardier.class, PricklyBoggart.class, Mutavault.class, Disperse.class})
class VioletPallTest extends BaseCardTest {

    // "Destroy target nonblack creature. Create a 1/1 black Faerie Rogue creature token with flying."

    @Test
    @DisplayName("Destroys a nonblack creature and creates a flying Faerie Rogue token")
    void destroysNonblackCreatureAndCreatesToken() {
        harness.addToBattlefield(player2, new BurrentonBombardier());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID bombardierId = harness.getPermanentId(player2, "Burrenton Bombardier");
        harness.castAndResolveInstant(player1, 0, bombardierId);

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
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player2, "Faerie Rogue")).isZero();
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

    @Test
    @DisplayName("Can destroy your own nonblack creature and still creates the token")
    void canDestroyOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Burrenton Bombardier");
        harness.assertInGraveyard(player1, "Burrenton Bombardier");
        assertThat(countPermanents(player1, "Faerie Rogue")).isEqualTo(1);
    }

    @Test
    @DisplayName("Can destroy a colorless animated land creature")
    void destroysAnimatedMutavault() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Mutavault");
        harness.assertInGraveyard(player1, "Mutavault");
        assertThat(countPermanents(player1, "Faerie Rogue")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no token when its only target leaves before resolution")
    void createsNoTokenWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());
        harness.setHand(player1, List.of(new VioletPall()));
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Burrenton Bombardier");
        harness.assertInGraveyard(player1, "Violet Pall");
        assertThat(countPermanents(player1, "Faerie Rogue")).isZero();
        assertThat(countPermanents(player2, "Faerie Rogue")).isZero();
    }
}
