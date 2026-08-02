package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "153")
public class DeathpactAngel extends Card {

    private static final String NAME = "Deathpact Angel";

    public DeathpactAngel() {
        final ActivatedAbility tokenAbility = new ActivatedAbility(
                true,
                "{3}{W}{B}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardNamedPredicate(NAME))
                                .build()
                ),
                "{3}{W}{B}{B}, {T}, Sacrifice this token: Return a card named " + NAME
                        + " from your graveyard to the battlefield."
        );

        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                CardType.CREATURE,
                1,
                "Cleric",
                1,
                1,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.CLERIC),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(tokenAbility),
                false,
                false,
                false,
                0,
                Set.<Keyword>of()
        ));
    }
}
