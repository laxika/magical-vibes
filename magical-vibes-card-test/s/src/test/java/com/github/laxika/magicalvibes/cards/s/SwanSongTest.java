package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.h.HeliodGodOfTheSun;
import com.github.laxika.magicalvibes.cards.r.ReadTheBones;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwanSong.class, MightOfOaks.class, GrizzlyBears.class, AngelicChorus.class,
        ReadTheBones.class, HeliodGodOfTheSun.class})
class SwanSongTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant spell and creates a 2/2 blue flying Bird")
    void countersInstantAndCreatesBird() {
        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.setHand(player1, List.of(mightOfOaks));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SwanSong()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mightOfOaks.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertNotOnBattlefield(player2, "Bird");
        List<Permanent> birds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bird"))
                .toList();
        assertThat(birds).hasSize(1);
        Permanent bird = birds.getFirst();
        assertThat(bird.getCard().getPower()).isEqualTo(2);
        assertThat(bird.getCard().getToughness()).isEqualTo(2);
        assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can target an enchantment spell")
    void countersEnchantmentSpell() {
        AngelicChorus angelicChorus = new AngelicChorus();
        harness.setHand(player1, List.of(angelicChorus));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.setHand(player2, List.of(new SwanSong()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, angelicChorus.getId());

        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Bird");
        harness.assertNotOnBattlefield(player2, "Bird");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setHand(player1, List.of(grizzlyBears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SwanSong()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, grizzlyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a sorcery and gives its controller the Bird without resolving the sorcery")
    void countersSorcerySpell() {
        ReadTheBones sorcery = new ReadTheBones();
        harness.setHand(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new SwanSong()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sorcery.getId());

        harness.assertInGraveyard(player1, "Read the Bones");
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Bird");
        harness.assertNotOnBattlefield(player2, "Bird");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's own spell and create a Bird")
    void countersOwnSpell() {
        MightOfOaks instant = new MightOfOaks();
        harness.setHand(player1, List.of(instant, new SwanSong()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, instant.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertOnBattlefield(player1, "Bird");
        harness.assertNotOnBattlefield(player2, "Bird");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter an enchantment creature spell")
    void countersEnchantmentCreatureSpell() {
        HeliodGodOfTheSun heliod = new HeliodGodOfTheSun();
        harness.setHand(player1, List.of(heliod));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player2, List.of(new SwanSong()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, heliod.getId());

        harness.assertInGraveyard(player1, "Heliod, God of the Sun");
        harness.assertNotOnBattlefield(player1, "Heliod, God of the Sun");
        harness.assertOnBattlefield(player1, "Bird");
        harness.assertNotOnBattlefield(player2, "Bird");
    }
}
