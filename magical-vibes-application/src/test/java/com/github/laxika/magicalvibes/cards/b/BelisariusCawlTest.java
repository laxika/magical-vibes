package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelisariusCawl.class, Ornithopter.class, GrizzlyBears.class, IzzetCluestone.class, Plains.class})
class BelisariusCawlTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two artifacts creates a vigilant Astartes Warrior")
    void tappingArtifactsCreatesAstartesWarrior() {
        Permanent cawl = addReady(new BelisariusCawl());
        Permanent artifact1 = addReady(new Ornithopter());
        Permanent artifact2 = addReady(new Ornithopter());
        Permanent creature = addReady(new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), null, null);
        harness.passBothPriorities();

        assertThat(cawl.isTapped()).isTrue();
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Astartes Warrior");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping X creatures looks at X cards and offers an artifact")
    void tappingCreaturesLooksAtXCardsAndOffersArtifact() {
        Permanent cawl = addReady(new BelisariusCawl());
        Permanent creature1 = addReady(new GrizzlyBears());
        Permanent creature2 = addReady(new GrizzlyBears());
        Card nonArtifact = new GrizzlyBears();
        Card artifact = new IzzetCluestone();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(nonArtifact, artifact, land));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), 1, 2, null);

        assertThat(cawl.isTapped()).isTrue();
        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(nonArtifact, artifact);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The creature-tapping ability excludes Belisarius Cawl from X")
    void creatureTappingCostExcludesSource() {
        Permanent cawl = addReady(new BelisariusCawl());
        Permanent creature = addReady(new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cawl), 1, 1, null);

        assertThat(cawl.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
