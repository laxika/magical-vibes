package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianRebirth.class, GrizzlyBears.class, SerraAngel.class})
class PhyrexianRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Rebirth destroys all creatures and creates a token with P/T equal to destroyed count")
    void destroysAllCreaturesAndCreatesToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        // All creatures destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Token created with P/T = 3 (three creatures destroyed)
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Phyrexian Horror");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Token is a colorless Phyrexian Horror artifact creature")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("With no creatures on battlefield, creates a 0/0 token")
    void noCreaturesCreatesZeroZeroToken() {
        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        // 0/0 token is created but dies to SBAs — should not be on battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("Indestructible creatures survive and are not counted for token P/T")
    void indestructibleCreaturesNotCounted() {
        GrizzlyBears indestructibleBears = new GrizzlyBears();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        harness.addToBattlefield(player1, indestructibleBears);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Indestructible creature survives
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Opponent creatures destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");

        // Token P/T = 2 (only 2 creatures were actually destroyed)
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroyed creatures go to their owners' graveyards")
    void destroyedCreaturesGoToGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Regenerated creatures survive and do not increase the Horror's size")
    void regeneratedCreaturesNotCounted() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent regenerated = gd.playerBattlefields.get(player1.getId()).getFirst();
        regenerated.setRegenerationShield(1);
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(regenerated.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Serra Angel");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @CardUsed({IchorWellspring.class})
    @DisplayName("Noncreature artifacts survive and do not count toward the Horror's size")
    void noncreatureArtifactsNotDestroyedOrCounted() {
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Ichor Wellspring");
        harness.assertNotInGraveyard(player1, "Ichor Wellspring");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A destroyed creature token contributes one to a subsequent Horror's size")
    void destroyedTokensCountAsCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhyrexianRebirth(), new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 12);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player2, new SerraAngel());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent replacement = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(replacement.getId()).isNotEqualTo(originalToken.getId());
        assertThat(replacement.getCard().isToken()).isTrue();
        assertThat(replacement.getCard().getPower()).isEqualTo(2);
        assertThat(replacement.getCard().getToughness()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Serra Angel");
    }
}
