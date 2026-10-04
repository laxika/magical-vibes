package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WelcomeTheDarkness;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfRidicule.class, GrizzlyBears.class, Ornithopter.class, WelcomeTheDarkness.class})
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

    @Test
    void multipleEnchantmentDamageDealersTriggerEachSourceOnlyOnce() {
        addCreatureReady(player1, new FearOfRidicule());
        addCreatureReady(player1, new FearOfRidicule());
        addCreatureReady(player1, new FearOfRidicule());
        harness.setLibrary(player2, List.of(new FearOfRidicule(), new FearOfRidicule(),
                new FearOfRidicule(), new FearOfRidicule(), new FearOfRidicule(), new FearOfRidicule()));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fear of Ridicule")
                .stream().filter(permanent -> permanent.getCard().isToken())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void nonEnchantmentCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new FearOfRidicule());
        addCreatureReady(player1, new GrizzlyBears());
        FearOfRidicule libraryCreature = new FearOfRidicule();
        harness.setLibrary(player2, List.of(libraryCreature));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCreature);
        assertThat(countPermanents(player1, "Fear of Ridicule")).isEqualTo(1);
        assertThat(gd.findExiledCard(libraryCreature.getId())).isNull();
    }

    @Test
    void emptyLibraryCreatesNoToken() {
        addCreatureReady(player1, new FearOfRidicule());
        harness.setLibrary(player2, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fear of Ridicule")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void libraryWithoutCreatureCardsIsUnchanged() {
        addCreatureReady(player1, new FearOfRidicule());
        WelcomeTheDarkness noncreature = new WelcomeTheDarkness();
        harness.setLibrary(player2, List.of(noncreature));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.findExiledCard(noncreature.getId())).isNull();
        assertThat(countPermanents(player1, "Fear of Ridicule")).isEqualTo(1);
    }

    @Test
    void randomSelectionSkipsNoncreatureCards() {
        addCreatureReady(player1, new FearOfRidicule());
        WelcomeTheDarkness noncreature = new WelcomeTheDarkness();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(noncreature, creature));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().isToken()).isTrue();
    }

    @Test
    void artifactCreatureCopyIsOnlyAnEnchantmentCreatureAndRetainsFlying() {
        addCreatureReady(player1, new FearOfRidicule());
        Ornithopter libraryCreature = new Ornithopter();
        harness.setLibrary(player2, List.of(libraryCreature));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Ornithopter");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(gd.findExiledCard(libraryCreature.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
