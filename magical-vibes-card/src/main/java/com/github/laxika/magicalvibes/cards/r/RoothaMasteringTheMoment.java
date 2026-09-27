package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "8")
public class RoothaMasteringTheMoment extends Card {

    public RoothaMasteringTheMoment() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        GreatestManaValueAmongSpellsCastThisTurn greatestManaValue =
                new GreatestManaValueAmongSpellsCastThisTurn(instantOrSorcery, CountScope.CONTROLLER);

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellThisTurn(instantOrSorcery),
                new CreateTokenEffect(
                        CardType.CREATURE, new Fixed(1), "Elemental", greatestManaValue, greatestManaValue,
                        CardColor.BLUE, Set.of(CardColor.BLUE, CardColor.RED),
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING, Keyword.HASTE), Set.of(),
                        false, false, Map.of(), List.of(), false, false, false, 0, Set.of())));
    }
}
