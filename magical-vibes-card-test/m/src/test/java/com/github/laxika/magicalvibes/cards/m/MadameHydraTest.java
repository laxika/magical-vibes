package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadameHydra.class, DocOcksHenchmen.class, GrizzlyBears.class})
class MadameHydraTest extends BaseCardTest {

    @Test
    void createsVillainTokenWhenYouCastAVillainSpell() {
        harness.addToBattlefield(player1, new MadameHydra());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void doesNotCreateTokenWhenYouCastANonVillainSpell() {
        harness.addToBattlefield(player1, new MadameHydra());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsVillainSpell() {
        harness.addToBattlefield(player1, new MadameHydra());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DocOcksHenchmen()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    void createsAnUntappedTokenBeforeTheVillainSpellResolves() {
        harness.addToBattlefield(player1, new MadameHydra());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Doc Ock's Henchmen");
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isFalse();
        assertThat(tokens.getFirst().isAttacking()).isFalse();
    }

    @Test
    void doesNotTriggerWhenMadameHydraItselfIsCast() {
        harness.setHand(player1, List.of(new MadameHydra()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Madame Hydra");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
