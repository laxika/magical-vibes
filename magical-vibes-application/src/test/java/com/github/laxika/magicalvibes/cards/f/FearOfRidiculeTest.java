package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfRidicule.class, GrizzlyBears.class})
class FearOfRidiculeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives menace to your enchantment creatures and creates a modified copy after combat damage")
    void grantsMenaceAndCreatesModifiedCreatureCopy() {
        Permanent fear = addCreatureReady(player1, new FearOfRidicule());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears libraryCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryCreature));

        assertThat(gqs.hasKeyword(gd, fear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(gd.findExiledCard(libraryCreature.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(libraryCreature);
    }
}
