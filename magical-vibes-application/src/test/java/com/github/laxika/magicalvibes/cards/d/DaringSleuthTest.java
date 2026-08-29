package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaringSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when its controller sacrifices a Clue")
    void transformsWhenControllerSacrificesClue() {
        Permanent sleuth = addReadySleuth();

        sacrificeClue(player1);

        assertThat(sleuth.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when its controller sacrifices a non-Clue permanent")
    void doesNotTransformForNonClueSacrifice() {
        Permanent sleuth = addReadySleuth();
        Permanent cluestone = new Permanent(new IzzetCluestone());
        cluestone.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cluestone);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cluestone), 1, null, null);
        harness.passBothPriorities();

        assertThat(sleuth.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Bearer investigates after dealing combat damage to a player")
    void bearerInvestigatesOnCombatDamage() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);

        sleuth.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Bearer's prowess boosts it for a noncreature spell")
    void bearerHasProwess() {
        Permanent sleuth = addReadySleuth();
        sacrificeClue(player1);

        int powerBefore = gqs.getEffectivePower(gd, sleuth);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sleuth)).isEqualTo(powerBefore + 1);

        harness.passBothPriorities();
    }

    private Permanent addReadySleuth() {
        Permanent sleuth = new Permanent(new DaringSleuth());
        sleuth.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sleuth);
        return sleuth;
    }

    private void sacrificeClue(Player player) {
        addClueToken(player);
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        Permanent clue = battlefield.stream()
                .filter(permanent -> permanent.getCard().getName().equals("Clue"))
                .findFirst()
                .orElseThrow();
        harness.setLibrary(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.COLORLESS, 2);

        harness.activateAbility(player, battlefield.indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setColor(null);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
    }
}
