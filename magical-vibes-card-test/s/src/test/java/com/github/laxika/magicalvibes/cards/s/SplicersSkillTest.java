package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({SplicersSkill.class, Shock.class, GrizzlyBears.class})
class SplicersSkillTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 Phyrexian Golem artifact creature token")
    void createsGolemToken() {
        harness.setHand(player1, List.of(new SplicersSkill()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Splices onto an instant and stays in hand")
    void splicesOntoInstant() {
        Card shock = new Shock();
        SplicersSkill skill = new SplicersSkill();
        harness.setHand(player1, List.of(shock, skill));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skill);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot splice onto a permanent spell")
    void rejectsPermanentHost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new SplicersSkill()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be used when casting an instant or sorcery");
    }

    @Test
    @DisplayName("Splices onto a sorcery when the revealed card precedes the host in hand")
    void splicesOntoSorceryBeforeHostInHand() {
        SplicersSkill revealed = new SplicersSkill();
        harness.setHand(player1, List.of(revealed, new SplicersSkill()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithSplice(player1, 1, null, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Each distinct spliced copy adds a token and requires its own splice payment")
    void splicesMultipleCopies() {
        SplicersSkill first = new SplicersSkill();
        SplicersSkill second = new SplicersSkill();
        harness.setHand(player1, List.of(new SplicersSkill(), first, second));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castWithSplice(player1, 0, null, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot splice the same physical card twice onto one spell")
    void rejectsDuplicateSpliceCard() {
        harness.setHand(player1, List.of(new SplicersSkill(), new SplicersSkill()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Paying the host's mana cost alone cannot pay for splice")
    void rejectsUnpaidSpliceCost() {
        harness.setHand(player1, List.of(new SplicersSkill(), new SplicersSkill()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Spliced token creation still happens when the host kills its target")
    void createsTokenWhenHostKillsTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        SplicersSkill skill = new SplicersSkill();
        harness.setHand(player1, List.of(new Shock(), skill));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skill);
    }

    @Test
    @DisplayName("No token is created if the host's only target becomes illegal before resolution")
    void doesNotCreateTokenWhenHostCannotResolve() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        SplicersSkill skill = new SplicersSkill();
        harness.setHand(player1, List.of(new Shock(), skill));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skill);
        harness.assertInGraveyard(player1, "Shock");
    }
}
