package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OrmendahlProfanePrince;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "287")
@CardRegistration(set = "INR", collectorNumber = "474")
public class WestvaleAbbey extends Card {

    public WestvaleAbbey() {
        setBackFaceCard(new OrmendahlProfanePrince());

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {5}, {T}, Pay 1 life: Create a 1/1 white and black Human Cleric creature token.
        addActivatedAbility(new ActivatedAbility(
                true, "{5}",
                List.of(
                        new PayLifeCost(1),
                        new CreateTokenEffect(1, "Human Cleric", 1, 1,
                                CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                                List.of(CardSubtype.HUMAN, CardSubtype.CLERIC))
                ),
                "{5}, {T}, Pay 1 life: Create a 1/1 white and black Human Cleric creature token."
        ));

        // {5}, {T}, Sacrifice five creatures: Transform this land, then untap it.
        addActivatedAbility(new ActivatedAbility(
                true, "{5}",
                List.of(
                        new SacrificeMultiplePermanentsCost(5, new PermanentIsCreaturePredicate()),
                        new TransformSelfEffect(),
                        new UntapPermanentsEffect(TapUntapScope.SELF)
                ),
                "{5}, {T}, Sacrifice five creatures: Transform this land, then untap it."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "OrmendahlProfanePrince";
    }
}
