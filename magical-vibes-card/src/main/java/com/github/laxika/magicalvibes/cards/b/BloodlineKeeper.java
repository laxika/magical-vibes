package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LordOfLineage;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "90")
public class BloodlineKeeper extends Card {

    public BloodlineKeeper() {
        // Set up back face
        LordOfLineage backFace = new LordOfLineage();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // {T}: Create a 2/2 black Vampire creature token with flying.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new CreateCreatureTokenEffect(
                        "Vampire", 2, 2,
                        CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE),
                        Set.of(Keyword.FLYING),
                        Set.of()
                )),
                "{T}: Create a 2/2 black Vampire creature token with flying."
        ));

        // {B}: Transform Bloodline Keeper. Activate only if you control five or more Vampires.
        addActivatedAbility(new ActivatedAbility(
                false, "{B}",
                List.of(new TransformSelfEffect()),
                "{B}: Transform Bloodline Keeper. Activate only if you control five or more Vampires.",
                CardSubtype.VAMPIRE, 5
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "LordOfLineage";
    }
}
