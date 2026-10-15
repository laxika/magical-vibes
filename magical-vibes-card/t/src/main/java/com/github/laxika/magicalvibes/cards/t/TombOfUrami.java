package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllMatchingPermanentsCost;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "165")
public class TombOfUrami extends Card {

    public TombOfUrami() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.BLACK),
                        new ConditionalEffect(
                                new ControlsPermanentCountAtMost(0, new PermanentHasSubtypePredicate(CardSubtype.OGRE)),
                                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER))
                ),
                "{T}: Add {B}. Tomb of Urami deals 1 damage to you if you don't control an Ogre."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}{B}",
                List.of(
                        new SacrificeAllMatchingPermanentsCost(new PermanentIsLandPredicate()),
                        new CreateTokenEffect(
                                CardType.CREATURE,
                                1,
                                "Urami",
                                5,
                                5,
                                CardColor.BLACK,
                                null,
                                List.of(CardSubtype.DEMON, CardSubtype.SPIRIT),
                                Set.of(Keyword.FLYING),
                                Set.of(),
                                false,
                                false,
                                Map.of(),
                                List.of(),
                                false,
                                false,
                                true,
                                0,
                                Set.of()
                        )
                ),
                "{2}{B}{B}, {T}, Sacrifice all lands you control: Create Urami, a legendary 5/5 black Demon Spirit creature token with flying."
        ));
    }
}
