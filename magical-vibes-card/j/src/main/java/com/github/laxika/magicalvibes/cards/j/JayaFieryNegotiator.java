package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedAttackDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "133")
public class JayaFieryNegotiator extends Card {

    public JayaFieryNegotiator() {
        // +1: Create a 1/1 red Monk creature token with prowess.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Monk", 1, 1, CardColor.RED,
                        List.of(CardSubtype.MONK), Set.of(Keyword.PROWESS), Set.of())),
                "+1: Create a 1/1 red Monk creature token with prowess."
        ));

        // −1: Exile the top two cards of your library. Choose one of them. You may play that card this turn.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new ExileTopCardsChooseOneMayPlayThisTurnEffect(2)),
                "−1: Exile the top two cards of your library. Choose one of them. You may play that card this turn."
        ));

        // −2: Choose target creature an opponent controls. Whenever you attack this turn, Jaya deals
        // damage equal to the number of attacking creatures to that creature.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RegisterDelayedAttackDamageEffect()),
                "−2: Choose target creature an opponent controls. Whenever you attack this turn, Jaya deals "
                        + "damage equal to the number of attacking creatures to that creature.",
                TargetFilters.creatureAnOpponentControls()
        ));

        CardAllOfPredicate redInstantOrSorcery = new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.RED),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))));

        // −8: You get an emblem with "Whenever you cast a red instant or sorcery spell, copy it twice.
        // You may choose new targets for the copies."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(
                                new CopyControllerCastSpellOnSpellCastEffect(redInstantOrSorcery, null, null),
                                new CopyControllerCastSpellOnSpellCastEffect(redInstantOrSorcery, null, null)),
                        "Whenever you cast a red instant or sorcery spell, copy it twice. You may choose new targets for the copies.")),
                "−8: You get an emblem with \"Whenever you cast a red instant or sorcery spell, copy it twice. You may choose new targets for the copies.\""
        ));
    }
}
